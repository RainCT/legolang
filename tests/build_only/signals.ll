print "Ready!"

on signal any_button_pressed: print "Button pressed!"

on signal enter_button_pressed: print "Enter pressed!"
on signal left_button_pressed: print "Left button pressed!"
on signal right_button_pressed: print "Right button pressed!"
on signal escape_button_pressed: print "Escape button pressed!"

on signal onTouch(S2): print "touched!!!"

on signal onRange20(S4): print "=/= range =/= " + signal_data

while true:
    pass
