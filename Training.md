Training

Download
wget http://sourceforge.net/projects/cmusphinx/files/sphinx3/0.8/sphinx3-0.8.tar.gz/download
wget http://cmusphinx.org/download/nightly/SphinxTrain.nightly.tar.gz

tar -xzf download
tar -xzf SphinxTrain.nightly.tar.gz

Jetzt libsphinxbase über repo man installieren

cd sphinx3-0.8/
./configure

make
sudo make install

cd ..
cd SphinxTrain/
./configure
make
cd ..


---

mkdir speech2chess01
cd speech2chess01
../SphinxTrain/scripts\_pl/setup\_SphinxTrain.pl --task speech2chess01

---

collecting data
kopieren der aufgenommenen wavx nach wav/
erzeugen von speech2chess01\_train.fileids muss nach etc/
>> ls  wav/ | sed "s/.wav//g" > etc/speech2chess01\_train.fileids
Kopiere speech2chess01.corpus das zum erstellen der wav benuttz wurde nach etc


---

Webseite zum erstellen vieler Dateien
Hinweis: hab festgestellt das die webseite das nur für english macht.
=> dic und phone muss selber erstellt werden.
http://www.speech.cs.cmu.edu/tools/lmtool-new.html

“Sentence corpus file:” field.  Then click “Compile Knowledge Base” and wait a few seconds for the results
Download gzip'd tar file
zahl.sent umbenennen nach speech2chess01\_train.transcription
>> cd etc
>> mv **.sent speech2chess01\_train.transcription
>> mv**.dic speech2chess01.dic

../transcription.sh etc/speech2chess01\_train.transcription mb

>> gedit speech2chess01.filler
<s>     SIL<br>
<br>
<br>
<sil><br>
<br>
   SIL<br>
<br>
<br>
Unknown end tag for </s><br>
<br>
    SIL<br>
<br>
<hr />
Über<br>
<a href='http://bakuzen.com/extractphoneme.php'>http://bakuzen.com/extractphoneme.php</a>
Phoneme erzeugen<br>
gedit speech2chess01.phone<br>
Mit dem Inhalt aber<br>
Achtung: Doppelte löschen<br>
Und SIL am ende hinzufügen fals noch nicht vorhanden<br>
Config file anpassen<br>
>> gedit sphinx_train.cfg<br>
$CFG_WAVFILE_EXTENSION = 'wav';<br>
$CFG_WAVFILE_TYPE = 'mswav';<br>
<br>
cd ..<br>
<hr />

./scripts_pl/make_feats.pl -ctl etc/speech2chess01_train.fileids<br>
./scripts_pl/RunAll.pl<br>
<br>
<hr />
Hinweise: Ich habe festgestellt das die Sprachwortschätze extrem groß sein müssen.<br>
Für einen Wortschatz der nur a1 - h8 erkennt brauchte ich 80 Aufnahmen, wobei die Zahlen 2 mal aufgenommen werden mussten und die buchstaben 3 mal, a und h hab ich gesondert nochmal aufgenommen, leider ohne erfolg.<br>
Sobald man nun "nach" auch mit aufnimmt werden die ergebnisse spürbar schlechter.<br>
Vermutung ist das ja sphinxtrain auch eine wahrscheinlichkeits berechnung macht und da nach nie im zusammenhang mit den anderen wörtern auftaucht ist es unwarscheinlich und wird deswegen seltenst erkannt.<br>
d.h. wir brauchen nochmal aufnahmen wo nach für jedes feld einmal vorne und einmal hinten auftaucht -> nochmal min 32 aufnahmen, warscheinlich eher sogar 64.