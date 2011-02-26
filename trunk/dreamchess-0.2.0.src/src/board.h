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

#ifndef BOARD_H
#define BOARD_H

#include <stdio.h>

#define PAWN 0
#define WHITE_PAWN 0
#define BLACK_PAWN 1
#define KNIGHT 2
#define WHITE_KNIGHT 2
#define BLACK_KNIGHT 3
#define BISHOP 4
#define WHITE_BISHOP 4
#define BLACK_BISHOP 5
#define ROOK 6
#define WHITE_ROOK 6
#define BLACK_ROOK 7
#define QUEEN 8
#define WHITE_QUEEN 8
#define BLACK_QUEEN 9
#define KING 10
#define WHITE_KING 10
#define BLACK_KING 11
#define NONE 12

#define CHAR_KING 16
#define CHAR_QUEEN 17
#define CHAR_ROOK 18
#define CHAR_KNIGHT 19
#define CHAR_BISHOP 20
#define CHAR_PAWN 21

#define IS_WHITE(P) (!((P) & 1))
#define IS_BLACK(P) ((P) & 1)

#define COLOUR(P) ((P) & 1)
#define PIECE(P) ((P) & 0xe)

#define OPPONENT(P) (1 - (P))

#define WHITE 0
#define BLACK 1

/* Move types. */
#define NORMAL 0
#define CAPTURE 1
#define QUEENSIDE_CASTLE 2
#define KINGSIDE_CASTLE 3

/* Move flags. */
#define MOVE_NORMAL 0
#define MOVE_CHECK 1
#define MOVE_CHECKMATE 2
#define MOVE_STALEMATE 3

/* Board flags. */
#define BOARD_NORMAL 0
#define BOARD_CHECK 1
#define BOARD_CHECKMATE 2
#define BOARD_STALEMATE 3

typedef struct board
{
    int turn;
    int square[64];
    int captured[10];
    int state;
}
board_t;

typedef struct move
{
    /* Source and destination squares. */
    int source, destination;
    /* Promotion piece (if any). */
    int promotion_piece;
    /* Move type. */
    int type;
    /* Board state after move. */
    int state;
}
move_t;

#define RESULT_WHITE_WINS 0
#define RESULT_BLACK_WINS 1
#define RESULT_DRAW 2

typedef struct result
{
    int code;
    char *reason;
}
result_t;

void board_setup(board_t *board);
char *move_to_fullalg(board_t *board, move_t *move);
move_t *fullalg_to_move(board_t *board, char *move_s);
move_t *san_to_move(board_t *board, char *move_s);
int make_move(board_t *board, move_t *move);
void move_set_attr(board_t *b, move_t *move);
int move_is_valid(board_t *b, move_t *move);
int move_is_semi_valid(board_t *board, move_t *move);
char *move_to_san(board_t *board, move_t *move);
char* san_to_fan(board_t *board, char *move_s);

#endif /* BOARD_H */
