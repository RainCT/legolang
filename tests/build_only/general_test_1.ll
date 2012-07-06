default touch S2

move(500, motorA)
delay(5000)
move(-500, motorA)
delay(10000)
stop(motorA)

// Reading lights
int i = 0;
while (i < 10):
	print readColor(S1)
	i = i+1

// Ultrasonic scan
i = 0
print "Range detector"
delay(3000)
while i < 10 :
	int range = rangeScan(50, S4)
	if range != -1:
		print "Something detected at: " + range + " cm"
	else:
		print "Nothing detected"
	waitForButton()
	i = i + 1

// Touch detector
i = 0;
while (i < 10):
	if isTouching():
		print "touched"
	else:
		print "not touched"
	i = i + 1

move(-200, motorA)
while !isTouching():
	print "not touched"

print "Press a button"
waitForButton()
