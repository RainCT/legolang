default ultrasonic S4
default light S3

global bool stop = false
global int speed = 500
global int turnSpeed = speed-100

on signal any_button_pressed:
    stop = true
    
print "Moving around!" at 0, 0

setLamp(GREEN)
move(speed, motorA); move(speed, motorC)

while not stop:
    if rangeScan(40) != -1 : sthg_detected();

stop(motorA); stop(motorC)
setLamp(BLUE)
print "Stopped" at 2, 4
waitForButton()

def sthg_detected():
    stop(motorA); stop(motorC)
    setLamp(RED)
    print "Close Enough!" at 2, 4
    delay(100)
    if random(1) == 1 :
        move(turnSpeed, motorA)
        move(-turnSpeed, motorC)
    else :
        move(-turnSpeed, motorA)
        move(turnSpeed, motorC)
    while rangeScan(50) != -1 :
        pass
    stop(motorA); stop(motorC)
    setLamp(GREEN)
    move(speed, motorA); move(speed, motorC)
    print "                " at 2, 4
