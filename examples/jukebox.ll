global int song = 1
global int[] song1
global int[] song2
global int[] song3

song1 = int[
94, 38, 330, 13, 392, 13, 0, 13,
392, 13, 0, 13, 330, 4, 0, 8, 294, 4, 0, 8, 196, 4, 0, 8, 247, 4,
0, 8, 294, 4, 0, 8, 247, 4, 0, 8, 330, 13, 0, 13, 294, 4, 0, 8,
247, 4, 0, 8, 220, 4, 0, 8, 247, 4, 0, 8, 196, 25, 220, 13, 233,
13, 247, 13, 294, 13, 330, 13, 294, 13, 392, 13, 0, 50, 294, 4, 0,
8, 330, 4, 0, 8, 294, 4, 0, 8, 392, 17, 0, 8, 392, 17, 0, 8, 330,
4, 0, 8, 294, 4, 0, 8, 196, 4, 0, 8, 247, 13, 294, 4, 0, 8, 247, 4,
0, 8, 330, 13, 0, 13, 294, 4, 0, 8, 247, 4, 0, 8, 196, 4, 0, 8,
247, 6, 0, 7, 294, 13, 0, 13, 294, 4, 0, 8, 294, 4, 0, 8, 370]

song2 = int[
784,15, 784,15, 784,15, 622,38, 698,15, 698,15, 698,15, 587,38, 784,15,
784,15, 784,15, 622,15, 831,15, 831,15, 831,15, 784,15, 1244,15, 1244,15,
1244,15, 1047,38, 784,15, 784,15, 784,15, 587,15, 831,15, 831,15, 831,15,
784,15, 1397,15, 1397,15, 1397,15, 1175,38, 1568,15, 1568,15, 1397,15,
1244,19, 1175,15, 1568,15, 1568,15, 1397,15, 1244,19, 1175,15, 1568,15,
1568,15, 1397,15, 1244,19, 1047,19, 784,38,0]

song3 = int[
523,40,  587,40,  659,40,  523,40,  523,40,  587,40,  659,40,  523,40, 
659,40,  698,40,  784,80,  659,40,  698,40,  784,80,  784,20,  880,20,
784,20,  698,20,  659,40,  523,40,  784,20,  880,20,  784,20,  698,20, 
659,40,  523,40,  523,40,  392,40,  523,80,  523,40,  392,40,  523,80]

def play(int[] Song):
    for i in 0 .. #Song/2 - 1:
        int tone = Song[2*i]
        int length = 10 * Song[2*i+1]
        playTone(tone, length)
        delay(length)

on signal left_button_pressed:
    if song == 1: song = 3
    else if song == 2: song = 1
    else if song == 3: song = 2

on signal right_button_pressed:
    if song == 1: song = 2
    else if song == 2: song = 3
    else if song == 3: song = 1

on signal enter_button_pressed:
    if song == 1: play(song1)
    else if song == 2: play(song2)
    else if song == 3: play(song3)

on signal escape_button_pressed:
    abort()

while true:
    clear()
    print "Song " + song
    delay(100)
