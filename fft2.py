import numpy as np
import math

# https://www.geeksforgeeks.org/how-to-extract-frequency-associated-with-fft-values-in-python/
# https://www.youtube.com/watch?v=s2K1JfNR7Sc

# Opens file to read
file = open("transfer.txt", "r+")

# Creates a list of the lines

lines = file.readlines().copy()

# Erases content in file
file.truncate(0)
file.close()
file = open("transfer.txt", "r+")
for line in lines:
    list = line.split() # Splits
    numberlist = []
    toWrite = ""
    for i in list:
        numberlist.append(int(i))

    x = np.array(numberlist)
    length = len(x)
    fhat = np.fft.fft(x)
    PSD = fhat * np.conj(fhat) / length
    freqs = np.fft.fftfreq(len(x))

    # Keeps track of the largest three numbers
    frequencies = [0, 0, 0, 0, 0, 0] # Frequencies of notes (NOTE: Length is 6, so it will take the first 6 frequencies)
    power = [0, 0, 0, 0, 0, 0] # Strength of notes
    for i in range(1, length):
        if freqs[i] > 0.00028: # 0.00028 is the lowest frequency
            if freqs[i] < 0.0448: # 0.0448 is the highest frequency at this scale on a standard piano
                # if PSD[i].real > x1[7]:
                for x in range(len(power)):
                    if PSD[i].real > power[x]:
                        power[x] = PSD[i].real # Replace power
                        frequencies[x] = freqs[i] # Replace frequency
                        break
            else:
                break

    for freq in frequencies:
        toWrite += str(freq) + " "

    file.write(toWrite + "\n")

file.close()
# compute frequency associated
# with coefficients
# print(freqs)

# print(freqs)
