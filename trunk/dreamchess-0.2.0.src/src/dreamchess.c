/*  DreamChess
**
**  DreamChess is the legal property of its developers, whose names are too
**  numerous to list here. Please refer to the COPYRIGHT file distributed
**  with this source distribution.
**
**  This program is free software: you can redistribute it and/or modify
**  it under the terms of the GNU General Public License as published by
**  the Free Software Foundation, either version 3 of the License, or
**  (at your option) any later version.
**
**  This program is distributed in the hope that it will be useful,
**  but WITHOUT ANY WARRANTY; without even the implied warranty of
**  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
**  GNU General Public License for more details.
**
**  You should have received a copy of the GNU General Public License
**  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif /* HAVE_CONFIG_H */

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>
#ifdef HAVE_GETOPT_H
#include <getopt.h>
#endif /* HAVE_GETOPT_H */
#include <errno.h>

#include "board.h"
#include "history.h"
#include "ui.h"
#include "comm.h"
#include "dir.h"
#include "dreamchess.h"
#include "debug.h"
#include "svn_version.h"
#include "audio.h"
#include "system_config.h"

#ifdef HAVE_GETOPT_LONG
#define OPTION_TEXT(L, S, T) "  " L "\t" S "\t" T "\n"
#else
#define OPTION_TEXT(L, S, T) "  " S "\t" T "\n"
#endif

/* FIXME */
int pgn_parse_file(char *filename);

typedef struct move_list
{
    char **move;
    int entries, view, max_entries;
}
move_list_t;

typedef struct cl_options {
	int width;
	int height;
	int fs;
	char *engine;
} cl_options_t;

static ui_driver_t *ui;
static config_t *config;
static move_list_t san_list, fan_list, fullalg_list;
static history_t *history;
static int in_game;
static int quit_game;
static int engine_error;

static void move_list_play(move_list_t *list, char *move)
{
    if (list->entries == list->max_entries)
    {
        list->max_entries *= 2;
        list->move = realloc(list->move, list->max_entries * sizeof(char *));
    }
    list->move[list->entries++] = strdup(move);
    list->view = list->entries - 1;
}

static void move_list_undo(move_list_t *list)
{
    if (list->entries > 0)
    {
        list->entries--;
        free(list->move[list->entries]);
        list->view = list->entries - 1;
    }
}

static void move_list_init(move_list_t *list)
{
    list->max_entries = 20;
    list->move = malloc(list->max_entries * sizeof(char *));
    list->entries = 0;
    list->view = -1;
}

static void move_list_exit(move_list_t *list)
{
    while (list->entries > 0)
        move_list_undo(list);
    free(list->move);
}

static void move_list_view_next(move_list_t *list)
{
    if (list->view < list->entries - 1)
        list->view++;
}

static void move_list_view_prev(move_list_t *list)
{
    if (list->view >= 0)
        list->view--;
}

void game_view_next()
{
    history_view_next(history);
    move_list_view_next(&fullalg_list);
    move_list_view_next(&san_list);
    move_list_view_next(&fan_list);
    ui->update(history->view->board, NULL);
}

void game_view_prev()
{
    history_view_prev(history);
    move_list_view_prev(&fullalg_list);
    move_list_view_prev(&san_list);
    move_list_view_prev(&fan_list);
    ui->update(history->view->board, NULL);
}

void game_undo()
{
    history_undo(history);
    move_list_undo(&fullalg_list);
    move_list_undo(&san_list);
    move_list_undo(&fan_list);
    if (history->result)
    {
        free(history->result->reason);
        free(history->result);
        history->result = NULL;
    }
    ui->update(history->view->board, NULL);
}

void game_retract_move()
{
    /* Make sure a user is on move and we can undo two moves. */
    if (config->player[history->last->board->turn] != PLAYER_UI)
        return;
    if (!history->last->prev || !history->last->prev->prev)
        return;

    game_undo();
    game_undo();
    comm_send("remove\n");
}

void game_move_now()
{
    /* Make sure engine is on move. */
    if (config->player[history->last->board->turn] != PLAYER_ENGINE)
        return;

    comm_send("?\n");
}

int game_want_move()
{
    return config->player[history->last->board->turn] == PLAYER_UI
           && history->last == history->view;
}

int game_save( int slot )
{
    int retval;
    char temp[80];

    if (!ch_userdir())
    {
        sprintf( temp, "save%i.pgn", slot );
        retval = history_save_pgn(history, temp);
    }
    else
    {
        DBG_ERROR("failed to enter user directory");
        retval = 1;
    }

    return retval;
}

void game_set_engine_error(int err)
{
    engine_error = err;
}

int game_get_engine_error()
{
    return engine_error;
}

static int do_move(move_t *move, int ui_update)
{
DBG_LOG("-> %s", __func__);
    char *move_s, *move_f, *move_san;
    board_t new_board;

    if (!move_is_valid(history->last->board, move))
    {
        DBG_WARN("move is illegal");
        return 0;
    }

    move_set_attr(history->last->board, move);
    new_board = *history->last->board;
    move_s = move_to_fullalg(&new_board, move);
    move_list_play(&fullalg_list, move_s);

    move_san = move_to_san(&new_board, move);
    move_f = san_to_fan(&new_board, move_san);

    DBG_LOG("processing move %s (%s)", move_s, move_san);

    move_list_play(&san_list, move_san);
    move_list_play(&fan_list, move_f);

    free(move_san);
    free(move_f);
    free(move_s);

    make_move(&new_board, move);

    if (move->state == MOVE_CHECK)
        new_board.state = BOARD_CHECK;
    else if (move->state == MOVE_CHECKMATE)
        new_board.state = BOARD_CHECKMATE;
    else
        new_board.state = BOARD_NORMAL;

    history_play(history, move, &new_board);

    if (ui_update)
        ui->update(history->view->board, move);

    if (new_board.state == MOVE_CHECKMATE)
    {
        history->result = malloc(sizeof(result_t));

        if (new_board.turn == WHITE)
        {
            history->result->code = RESULT_BLACK_WINS;
            history->result->reason = strdup("Black mates");
        }
        else
        {
            history->result->code = RESULT_WHITE_WINS;
            history->result->reason = strdup("White mates");
        }

        if (ui_update)
            ui->show_result(history->result);
    }
    else if (new_board.state == MOVE_STALEMATE)
    {
        history->result = malloc(sizeof(result_t));

        history->result->code = RESULT_DRAW;
        history->result->reason = strdup("Stalemate");

        if (ui_update)
            ui->show_result(history->result);
    }
DBG_LOG("-< %s", __func__);
    return 1;
}

void game_make_move(move_t *move, int ui_update)
{
DBG_LOG("-> %s", __func__);
    if (do_move(move, ui_update)){
        comm_send("%s\n", fullalg_list.move[fullalg_list.entries-1]);
    }
    else
    {
        char *move_str = move_to_fullalg(history->last->board, move);
        DBG_WARN("ignoring illegal move %s", move_str);
        free(move_str);
    }
DBG_LOG("-< %s", __func__);
}

void game_quit()
{
    in_game = 0;
}

int game_load( int slot )
{
    int retval;
    char temp[80];
    board_t *board;

    if (ch_userdir())
    {
        DBG_ERROR("failed to enter user directory");
        return 1;
    }

    comm_send("force\n");

    sprintf( temp, "save%i.pgn", slot );
    retval = pgn_parse_file(temp);

    if (retval)
    {
        DBG_ERROR("failed to parse PGN file '%s'", temp);
        return 1;
    }

    board = history->last->board;

    ui->update(board, NULL);

    if (config->player[board->turn] == PLAYER_ENGINE)
        comm_send("go\n");
    else if (config->player[OPPONENT(board->turn)] == PLAYER_ENGINE)
    {
        if (board->turn == WHITE)
            comm_send("white\n");
        else
            comm_send("black\n");
    }

    return retval;
}

void game_make_move_str(char *move_str, int ui_update)
{
DBG_LOG("-> %s", __func__);
    board_t new_board = *history->last->board;
    move_t *engine_move;

    DBG_LOG("parsing move string '%s'", move_str);

    engine_move = san_to_move(&new_board, move_str);

    if (!engine_move)
        engine_move = fullalg_to_move(&new_board, move_str);
    if (engine_move)
    {
        game_make_move(engine_move, ui_update);
        free(engine_move);
    }
    else
        DBG_ERROR("failed to parse move string '%s'", move_str);
DBG_LOG("-< %s", __func__);
}

void game_get_move_list(char ***list, int *total, int *view)
{
    *list = fan_list.move;
    *total = fan_list.entries;
    *view = fan_list.view;
}

int set_resolution(int init)
{
    int width, height, fs, ms;
    option_t *option = config_get_option("resolution");
    config_resolution_t *res = option->selected->data;

    option = config_get_option("full_screen");
    fs = option->selected->index;

    option = config_get_option("multisampling");

    ms = option->selected->index * 2;

    if (res) {
        width = res->w;
        height = res->h;
    }
    else {
        /* Custom */
        option = config_get_option("custom_resolution_width");
        width = option->value;
        option = config_get_option("custom_resolution_height");
        height = option->value;
    }

    if (init)
        return ui->init(width, height, fs, ms);
    else
        return ui->resize(width, height, fs, ms);
}

static void init_resolution()
{
    if (set_resolution(1)) {
        DBG_LOG("switching to failsafe video mode defaults");
        config_set_failsafe_video();
        config_save();

        if (set_resolution(1)) {
            exit(1);
        }
    }
}

void toggle_fullscreen()
{
#ifdef _WIN32
    DBG_WARN("fullscreen toggling is currently not supported on win32");
#else
    option_t *option = config_get_option("full_screen");
    option_select_value_by_index(option, 1 - option->selected->index);
    set_resolution(0);
#endif
}

#ifndef _arch_dreamcast
static void parse_options(int argc, char **argv, ui_driver_t **ui_driver, cl_options_t *cl_options)
{
    int c;

#ifdef HAVE_GETOPT_LONG

    int optindex;

    struct option options[] =
        {
            {"help", no_argument, NULL, 'h'
            },
            {"list-drivers", no_argument, NULL, 'l'},
            {"ui", required_argument, NULL, 'u'},
            {"fullscreen", no_argument, NULL, 'f'},
            {"width", required_argument, NULL, 'W'},
            {"height", required_argument, NULL, 'H'},
            {"1st-engine", required_argument, NULL, '1'},
            {"verbose", required_argument, NULL, 'v'},
            {0, 0, 0, 0}
        };

    while ((c = getopt_long(argc, argv, "1:fhlu:v:W:H:", options, &optindex)) > -1) {
#else

    while ((c = getopt(argc, argv, "1:fhlu:v:W:H:")) > -1) {
#endif /* HAVE_GETOPT_LONG */
        switch (c)
        {
        case 'h':
            printf("Usage: dreamchess [options]\n\n"
                   "An xboard-compatible chess interface.\n\n"
                   "Options:\n"
                   OPTION_TEXT("--help\t", "-h\t", "Show help.")
                   OPTION_TEXT("--list-drivers", "-l\t", "List all available drivers.")
                   OPTION_TEXT("--ui <drv>\t", "-u<drv>\t", "Use user interface driver <drv>.")
                   OPTION_TEXT("--fullscreen\t", "-f\t", "Run fullscreen")
                   OPTION_TEXT("--width\t", "-W<num>\t", "Set screen width")
                   OPTION_TEXT("--height\t", "-H<num>\t", "Set screen height")
                   OPTION_TEXT("--1st-engine <eng>", "-1<eng>\t", "Use <eng> as first chess engine.\n\t\t\t\t\t  Defaults to 'dreamer'.")
                   OPTION_TEXT("--verbose <level>", "-v<level>", "Set verbosity to <level>.\n\t\t\t\t\t  Verbosity levels:\n\t\t\t\t\t  0 - Silent\n\t\t\t\t\t  1 - Errors only\n\t\t\t\t\t  2 - Errors and warnings only\n\t\t\t\t\t  3 - All\n\t\t\t\t\t  Defaults to 1")
                  );
            exit(0);
        case 'l':
            printf("Available drivers:\n\n");
            ui_list_drivers();
            exit(0);
        case 'u':
            if (!(*ui_driver = ui_find_driver(optarg)))
            {
                DBG_ERROR("could not find user interface driver '%s'", optarg);
                exit(1);
            }
            break;
        case '1':
            cl_options->engine = optarg;
            break;
        case 'f':
            cl_options->fs = 1;
            break;
        case 'W':
            cl_options->width = atoi(optarg);
            break;
        case 'H':
            cl_options->height = atoi(optarg);
            break;
        case 'v':
            {
                int level;
                char *endptr;

                errno = 0;
                level = strtol(optarg, &endptr, 10);

                if (errno || (optarg == endptr) || (level < 0) || (level > 3))
                {
                    DBG_ERROR("illegal verbosity level specified");
                    exit(1);
                }

                dbg_set_level(level);
            }
        }
    }
}
#endif

static void set_cl_options(cl_options_t *cl_options)
{
        option_t *option;

	if (cl_options->engine) {
	    option = config_get_option("1st_engine");
	    option_string_set_text(option, cl_options->engine);
	}

	if (cl_options->fs) {
	    option = config_get_option("full_screen");
	    option_select_value_by_name(option, "On");
	}

	if (cl_options->width) {
	    option = config_get_option("custom_resolution_width");
	    option->value = cl_options->width;
	    option = config_get_option("resolution");
	    option_select_value_by_name(option, "Custom");
        }

        if (cl_options->height) {
	    option = config_get_option("custom_resolution_height");
	    option->value = cl_options->height;
	    option = config_get_option("resolution");
	    option_select_value_by_name(option, "Custom");
        }
}

#include <pthread.h> 
#ifdef __MINGW32__
#include <winsock.h>
#else
#include <arpa/inet.h>
#endif

typedef int socklen_t;

#define TCP_PORT 54000 
#define TCP_PORT2 54001 

#define REQ_MOVE 1
#define REQ_QUIT 20
#define REQ_RESTART 21
    
#define REQ_VERIFY 10
#define REQ_FIGURES 12
#define REQ_PRINT 13
#define REQ_PRINT2 14

int ready_to_send = 0;
int ready_to_send_len = 0;
unsigned char ready_to_send_buf[4096];
unsigned char ready_to_send_buf_type[1];

void * tcpRequests(void * none) 
{ 
		DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
		int fdc, fdd, len_local; 
        struct sockaddr_in local, remote; 
 
#ifdef __MINGW32__
WSADATA wsaData;
WSAStartup(MAKEWORD(2, 2), &wsaData);
#endif  /*  __MINGW32__  */
 
        if ((fdc=socket(PF_INET, SOCK_STREAM, 0)) < 0) { 
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
                perror("tcp socket error"); 
                pthread_exit(NULL); 
        } 
        len_local = sizeof(local); 
        memset(&local, 0, len_local); 
        local.sin_family = AF_INET; 
        local.sin_port = htons(TCP_PORT); 
        local.sin_addr.s_addr = htonl(INADDR_ANY); 
 
		DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
 
        // bind and listen on port and ip interface 
        if (bind(fdc, (struct sockaddr*)&local, len_local) < 0) { 
                perror("tcp bind error"); 
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
                pthread_exit(NULL); 
        } 
        if (listen(fdc, 10) < 0) { 
                perror("tcp listen error"); 
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
                pthread_exit(NULL); 
        } 

		while(1) { 

			DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
			
			unsigned char buffer[255];
			int rlen = sizeof(remote); 
			// accept connection 
//#ifdef __MINGW32__
			if ((fdd = accept(fdc, NULL, NULL)) <= 0) { 
//#else
			//if ((fdd = accept(fdc, (struct sockaddr*)&remote, (socklen_t*)&rlen)) <= 0) { 
//#endif
					DBG_LOG("accept error"); 
					continue; 
			} 

			DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
			//close(fdc); 

			DBG_LOG("Got TCP-Connection from %s", inet_ntoa(remote.sin_addr)); 

			//First of read what the client want 
#ifdef __MINGW32__
			int slen = recv(fdd, buffer, 1, 0);
#else
			int slen = read(fdd, buffer, 1);
#endif
			int type = buffer[0];
			DBG_LOG("Type: %d (%d)", type, slen);
			
			// Read Object Size
#ifdef __MINGW32__
			slen = recv(fdd, buffer, 2, 0);
#else
			slen = read(fdd, buffer, 2);
#endif
			int size = buffer[0] + (buffer[1] << 8);
			DBG_LOG("Size: %d (%d)", size, slen);
			
			// Read Object
#ifdef __MINGW32__
			slen = recv(fdd, buffer, size, 0);
#else
			slen = read(fdd, buffer, size);
#endif
			DBG_LOG("Object Size: %d", slen);
	
			switch(type)
			{
			case REQ_MOVE:
			{
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
				char str_move[size+1];
				memcpy(str_move, buffer, size);
				str_move[size] = '\0';
				DBG_LOG("Move: %s", str_move);
				game_make_move_str(str_move, 1);
				
				ready_to_send_len = 0;
				ready_to_send_buf_type[0] = REQ_MOVE;
				ready_to_send = 1;
			}
				break;
			case REQ_QUIT:
			{
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
				quit_game = 1;
				game_quit();
			}
				break;
			case REQ_RESTART:
			{
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
				game_quit();
			}
				break;
			case REQ_VERIFY:
			{
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
				char str_move[size+1];
				memcpy(str_move, buffer, size);
				str_move[size] = '\0';
				DBG_LOG("Test: %s", str_move);
				
				board_t board = *history->last->board;
				move_t *move = fullalg_to_move(&board, str_move);
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
				int result = move!=NULL?1:0; //move_is_semi_valid(&board, move);
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
				ready_to_send_len = 5;
				ready_to_send_buf_type[0] = REQ_VERIFY;
				ready_to_send_buf[0] = result & 0xFF;
				ready_to_send_buf[1] = str_move[0];
				ready_to_send_buf[2] = str_move[1];
				ready_to_send_buf[3] = str_move[2];
				ready_to_send_buf[4] = str_move[3];
				ready_to_send = 1;
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
				
				
			}
				break;
			case REQ_FIGURES:
			{
				unsigned char i = 0;
				unsigned char figure = buffer[0];
				ready_to_send_len = 0;
				board_t board = *history->last->board;
				for(i = 0; i < 64; i++) {
					if(board.square[i] == figure) {
						ready_to_send_buf[ready_to_send_len] = i;
						ready_to_send_len++;
					}
				}
				ready_to_send_buf_type[0] = REQ_FIGURES;
				ready_to_send = 1;
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
			}
				break;
			case REQ_PRINT:
			{
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
				char str_print[size+1];
				memcpy(str_print, buffer, size);
				str_print[size] = '\0';
				DBG_LOG("Print: %s", str_print);
				setUI(1, str_print);
			}
				break;
			case REQ_PRINT2:
			{
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
				char str_print[size+1];
				memcpy(str_print, buffer, size);
				str_print[size] = '\0';
				DBG_LOG("Print2: %s", str_print);
				setUI(2, str_print);
			}
				break;
			default:
				break;
			}
		}
				
}

void * tcpResponse(void * none) 
{ 
		DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
		int fdc, fdd, len_local; 
        struct sockaddr_in local, remote; 
 
#ifdef __MINGW32__
WSADATA wsaData;
WSAStartup(MAKEWORD(2, 2), &wsaData);
#endif  /*  __MINGW32__  */
 
        if ((fdc=socket(PF_INET, SOCK_STREAM, 0)) < 0) { 
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
                perror("tcp socket error"); 
                pthread_exit(NULL); 
        } 
        len_local = sizeof(local); 
        memset(&local, 0, len_local); 
        local.sin_family = AF_INET; 
        local.sin_port = htons(TCP_PORT2); 
        local.sin_addr.s_addr = htonl(INADDR_ANY); 
 
		DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
 
        // bind and listen on port and ip interface 
        if (bind(fdc, (struct sockaddr*)&local, len_local) < 0) { 
                perror("tcp bind error"); 
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
                pthread_exit(NULL); 
        } 
        if (listen(fdc, 10) < 0) { 
                perror("tcp listen error"); 
				DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
                pthread_exit(NULL); 
        } 

		while(1) { 
			DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
			
			unsigned char buffer[255];
			int rlen = sizeof(remote); 
			if ((fdd = accept(fdc, NULL, NULL)) <= 0) { 
					DBG_LOG("accept error"); 
					continue; 
			} 

			DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 

			DBG_LOG("Got TCP-Connection from %s", inet_ntoa(remote.sin_addr)); 

			DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
			while(!ready_to_send) {
				usleep(10000);
			}
			DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
			int slen = 0;
			
#ifdef __MINGW32__
			slen = send(fdd, ready_to_send_buf_type, 1, 0);
#else
			slen = write(fdd, ready_to_send_buf_type, 1);
#endif
			unsigned char data_len[2];
			data_len[0] = (unsigned char)((ready_to_send_len) >> 8);
			data_len[1] = (unsigned char)((ready_to_send_len) & 0xFF);

#ifdef __MINGW32__
			slen = send(fdd, data_len, 2, 0);
#else
			slen = write(fdd, data_len, 2);
#endif

			if ((ready_to_send_len) > 0) {

#ifdef __MINGW32__
				slen = send(fdd, ready_to_send_buf, ready_to_send_len, 0);
#else
				slen = write(fdd, ready_to_send_buf, ready_to_send_len);
#endif
			}
			ready_to_send = 0;
			
			DBG_LOG("%s:%d", __FUNCTION__, __LINE__); 
			close(fdd);
		}
}

int dreamchess(void *data)
{
    cl_options_t cl_options = {};

#ifndef _arch_dreamcast

    arguments_t *arg = data;
#endif

    ui = ui_driver[0];

    printf( "DreamChess " "v" PACKAGE_VERSION " (r" SVN_VERSION ")\n" );

#ifndef _arch_dreamcast

    parse_options(arg->argc, arg->argv, &ui, &cl_options);
#endif

    config_init();

    set_cl_options(&cl_options);

    if (!ui)
    {
        DBG_ERROR("failed to find a user interface driver");
        exit(1);
    }

    init_resolution();

	quit_game = 0;

    while (!quit_game)
    {
        board_t board;
        int pgn_slot;
        option_t *option;

        if (!(config = ui->config(&pgn_slot)))
            break;

//+++>
		// Lets rumble, start receiver thread
		pthread_t tcpRequestsThread; 
		pthread_create(&tcpRequestsThread, NULL, tcpRequests, NULL); 
		pthread_t tcpResponseThread; 
		pthread_create(&tcpResponseThread, NULL, tcpResponse, NULL); 
//+++<
			
			
        ch_userdir();
        option = config_get_option("1st_engine");

        game_set_engine_error(comm_init(option->string));

        comm_send("xboard\n");

        comm_send("new\n");
        comm_send("random\n");

        comm_send("sd %i\n", config->cpu_level);
        comm_send("depth %i\n", config->cpu_level);

        if (config->difficulty == 0)
	    comm_send("noquiesce\n");

        if (config->player[WHITE] == PLAYER_UI
	    && config->player[BLACK] == PLAYER_UI)
	    comm_send("force\n");

        if (config->player[WHITE] == PLAYER_ENGINE)
	    comm_send("go\n");

        in_game = 1;
        board_setup(&board);
        history = history_init(&board);
        move_list_init(&san_list);
        move_list_init(&fan_list);
        move_list_init(&fullalg_list);

        if (pgn_slot >= 0)
            if (game_load(pgn_slot))
            {
                 DBG_ERROR("failed to load savegame in slot %i", pgn_slot);
                 exit(1);
            }

        ui->update(history->view->board, NULL);
        while (in_game)
        {
            char *s;

            if ((s = comm_poll()))
            {
                DBG_LOG("message from engine: '%s'", s);
                if  (!history->result)
                {
                    if ((!strncmp(s, "move ", 4) || strstr(s, "... ")) && config->player[history->last->board->turn] == PLAYER_ENGINE)
                    {
                        char *move_str = strrchr(s, ' ') + 1;
                        board_t new_board = *history->last->board;
                        move_t *engine_move;

                        DBG_LOG("parsing move string '%s'", move_str);

                        engine_move = san_to_move(&new_board, move_str);
                        if (!engine_move)
                            engine_move = fullalg_to_move(&new_board, move_str);
                        if (engine_move)
                        {
                            audio_play_sound(AUDIO_MOVE);
DBG_LOG("<engine_move> %s", __func__);
                            do_move(engine_move, 1);
                            free(engine_move);
                        }
                        else
                            DBG_ERROR("failed to parse move string '%s'", move_str);
                    }
                    else if (strstr(s, "llegal move"))
                        game_undo();
                    /* Ignore result message if we've already determined a result ourselves. */
                    else
                    {
                        char *start = strchr(s, '{');
                        char *end = strchr(s, '}');

                        if (start && end && end > start)
                        {
                            char *comment = malloc(end - start);
                            history->result = malloc(sizeof(result_t));
                            strncpy(comment, start + 1, end - start - 1);
                            comment[end - start - 1] = '\0';
                            history->result->reason = comment;
                            if (strstr(s, "1-0"))
                            {
                                history->result->code = RESULT_WHITE_WINS;
                                ui->show_result(history->result);
                            }
                            else if (strstr(s, "1/2-1/2"))
                            {
                                history->result->code = RESULT_DRAW;
                                ui->show_result(history->result);
                            }
                            else if (strstr(s, "0-1"))
                            {
                                history->result->code = RESULT_BLACK_WINS;
                                ui->show_result(history->result);
                            }
                            else
                            {
                                free(history->result->reason);
                                free(history->result);
                                history->result = NULL;
                            }
                        }
                    }
                }

                free(s);
            }
            ui->poll();
        }
        comm_send("quit\n");
        comm_exit();
        history_exit(history);
        move_list_exit(&san_list);
        move_list_exit(&fan_list);
        move_list_exit(&fullalg_list);
    }
    ui->exit();
    return 0;
}
