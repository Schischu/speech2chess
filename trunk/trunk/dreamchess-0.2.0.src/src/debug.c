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

#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "debug.h"

#define STC
#ifdef STC

static FILE * sDebugFD = NULL;

void writeDebug(char * text)
{
	if(sDebugFD == NULL)
		sDebugFD = fopen("debug.txt", "w");
	fputs (text, sDebugFD);
	fflush (sDebugFD);
}

#endif

static int dbg_level = 3;

/* Debug levels:
** 0: Silent
** 1: Errors
** 2: Errors + Warnings
** 3: Errors + Warnings + Log
*/

void dbg_set_level(int level)
{
	writeDebug("[INF] dbg_set_level");
    dbg_level = level;
}

#ifdef HAVE_VARARGS_MACROS
void dbg_error(char *file, int line, const char *fmt, ...)
#else
void dbg_error(const char *fmt, ...)
#endif
{
    va_list ap;

    if (dbg_level < 1)
        return;

#ifdef STC
	writeDebug("[ERR] ");
#endif
		
#ifdef HAVE_VARARGS_MACROS
    fprintf(stderr, "%s:%d: ", file, line);
#ifdef STC
    fprintf(sDebugFD, "%s:%d: ", file, line);
#endif
#endif
    fprintf(stderr, "error: ");
#ifdef STC
    fprintf(sDebugFD, "error: ");
#endif

    va_start(ap, fmt);
    vfprintf(stderr, fmt, ap);
#ifdef STC
    vfprintf(sDebugFD, fmt, ap);
#endif
    va_end(ap);

    fprintf(stderr, "\n");
#ifdef STC
    writeDebug("\n");
#endif
}

#ifdef HAVE_VARARGS_MACROS
void dbg_warn(char *file, int line, const char *fmt, ...)
#else
void dbg_warn(const char *fmt, ...)
#endif
{
    va_list ap;

    if (dbg_level < 2)
        return;

#ifdef STC
	writeDebug("[WAR] ");
#endif
		
#ifdef HAVE_VARARGS_MACROS
    printf("%s:%d: ", file, line);
#ifdef STC
    fprintf(sDebugFD, "%s:%d: ", file, line);
#endif
#endif
    printf("warning: ");
#ifdef STC
    fprintf(sDebugFD, "warning: ");
#endif
    va_start(ap, fmt);
    vprintf(fmt, ap);
#ifdef STC
    vfprintf(sDebugFD, fmt, ap);
#endif
    va_end(ap);

    printf("\n");
#ifdef STC
    writeDebug("\n");
#endif
}

#ifdef HAVE_VARARGS_MACROS
void dbg_log(char *file, int line, const char *fmt, ...)
#else
void dbg_log(const char *fmt, ...)
#endif
{
    va_list ap;

    if (dbg_level < 3)
        return;

#ifdef STC
	writeDebug("[LOG] ");
#endif
		
#ifdef HAVE_VARARGS_MACROS
    printf("%s:%d: ", file, line);
#ifdef STC
    fprintf(sDebugFD, "%s:%d: ", file, line);
#endif
#endif

    va_start(ap, fmt);
    vprintf(fmt, ap);
#ifdef STC
    vfprintf(sDebugFD, fmt, ap);
#endif
    va_end(ap);

    printf("\n");
#ifdef STC
    writeDebug("\n");
#endif
}
