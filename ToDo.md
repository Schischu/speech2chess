# ToDo #

  * ~~Absturz~~
  * ~~Auflösung bei Fullscreen-Mode~~
  * ~~Starten auf einen Klick~~
  * Sprach-Aktivitäts-Rückmeldung
  * ~~Rochade/Roschade), kurze/lange~~
  * "Spiel aufgeben"
  * Umlaute
  * Buchstaben unten am Bildrand sind abgeschnitten (gluPerspective http://wiki.delphigl.com/index.php/gluPerspective, glViewport http://wiki.delphigl.com/index.php/glViewport)

`glTranslatef(0, 0,-6);` (Verschiebt die ganze "Welt" in eine Richtung, einer von den drei Werten schiebt alles nach hinten, muss man mal ausprobieren, welcher von den dreien und um wieviel)
Eingesetzt wird das ganze hier.

```
void go_3d(int width, int height)
{
    glViewport( 0, 0, width, height );

    glMatrixMode( GL_PROJECTION );
    glLoadIdentity();
    gluPerspective(45.0f, 640.0f/480.0f, 1.0f, 100.0f);
    glMatrixMode( GL_MODELVIEW );
    glLoadIdentity();
    // Zeile HIER einfügen
}
```

c:\mingw\msys1.0\msys.bat starten
dreamchess\src\./make.sh starten für build