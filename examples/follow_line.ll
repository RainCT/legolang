default light S3 // i S1

global float tolerance = 0.2
global int maxSpeed = 300 // 100

global bool leftInPath = true
global bool rightInPath = true

global bool seenRed = false

/*
print "WHITE PLEASE"
delay(1000)
calibrateWhite()

print "BLACK PLEASE"
delay(1000)
calibrateBlack()
*/

delay(1000)

def onPath(sensor s) returns bool:
    color c = readColor(s)
    print colorSimilarity(c, BLACK)
    return isColor(c, BLACK, tolerance)

def moveForward():
    move(maxSpeed, motorA)
    move(maxSpeed, motorC)

def moveBackward():
    move(-maxSpeed, motorA)
    move(-maxSpeed, motorC)

def moveLeft():
    move(250, motorA)
    move(50, motorC)

def moveRight():
    move(250, motorA)
    move(50, motorC)

leftInPath = onPath(S1)
rightInPath = onPath(S2)

on signal any_button_pressed:
    abort()

buzz();

while true:
    moveForward()
    bool left = onPath(S1)
    bool right = onPath(S3)
    if not left and not right:
        twoBeeps()
    else if leftInPath and not left:
        print "Moving right..."
        beepSequence();
        moveRight()
        while not left:
            left = onPath(S1)
    else if rightInPath and not right:
        print "Moving left..."
        beepSequenceUp();
        moveLeft()
        while not right:
            right = onPath(S3)
    else if not seenRed:
        // Look for red checkpoint
        color leftColor = readColor(S1)
        if isColor(leftColor, RED, 0.3):
            print "RED"
            seenRed = true
            for i in 1 .. 3:
                twoBeeps()
                if (i != 2): moveBackward()
                else: moveForward()
                delay(500)
