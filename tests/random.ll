def check():

    int x = random(5)
    if x < 0 or x > 5: print "fail a"

    x = random(0, 5)
    if x < 0 or x > 5: print "fail b"

    x = random(100, 101); // <- semi-colon
    if x < 100 or x > 101: print "fail c";

    x = random(-5, 5)
    if x < -5 or x > 5: print "fail d"

    x = random(-1000, -50)
    if x < -1000 or x > -50: print "fail e"

for i in 1 .. 100:
    check()

if random(0, 0) != 0: print "fail f"
if random(3, 3) != 3: print "fail g"
if random(-5, -5) != -5: print "fail h"
if random(100000000, 100000000) != 100000000: print "fail i"

print "OK!"

// Check that the borders are included
while random(0, 2) != 0: pass
while random(0, 2) != 2: pass
while random(3, 5) != 5: pass
while random(-3, -1) != -1: pass
while random(-3, -1) != -3: pass

print "Done!"
