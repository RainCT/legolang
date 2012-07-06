color c = RED
print c

c = GREEN
print c

c = BLUE
print c

c = #ffffff
print c

c = #FFFFFF
print c

c = #FFF
print c

c = #123
print c

c = #aabbcc
print c

print ""
print getRed(c)
print getGreen(c)
print getBlue(c)

print ""
print getRed(GREEN)
print getGreen(GREEN)
print getBlue(GREEN)

c = #fe01ff

print ""
print getRed(c)
print getGreen(c)
print getBlue(c)

print ""
print isColor(BLACK, BLACK, 0)
print isColor(BLACK, WHITE, 0)
print isColor(BLACK, WHITE, 1)
print isColor(BLACK, #000, 0)
print isColor(BLACK, #fff, 0)

print ""
print isColor(#fff, #eee, 0)
print isColor(#fff, #eee, 0.05)
print isColor(#fff, #eee, 0.1)

print ""
print isColor(#f00, #f00)
print isColor(#f00, #f01)
print isColor(#f00, #0f0)
print isColor(#f00, #f23)
print isColor(#f00, #f23, 0.2)

print ""
print colorSimilarity(RED, RED)
print round(colorSimilarity(RED, BLUE), 3)
print colorSimilarity(BLACK, BLACK)
print colorSimilarity(BLACK, WHITE)
print round(colorSimilarity(#ff0000, #ff2222), 3)
