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

#define SELECTOR_UP 0
#define SELECTOR_DOWN 1
#define SELECTOR_LEFT 2
#define SELECTOR_RIGHT 3

void move_camera(float x, float z);
void render_scene_3d(board_t *board, int reflections);
void move_selector(int direction);
int get_selector();
void select_piece(int square);
void reset_3d();
void loadmodels(char *filename);
void load_board(char *dcm_name, char *texture_name);
int find_square(int x, int y, float fd);
void freemodels();

