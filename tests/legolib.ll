int startTime = getUptime()
delay(200)

if getUptime() - startTime >= 200:
    print "OK"
else:
    print "Delay was too short!"
