import numpy as np
import keras
from keras.utils import to_categorical
from keras.models import Sequential,Model
from keras.layers import MaxPooling2D,Dropout,Flatten,Dense,MaxPooling1D, Conv2D
import tensorflow as tf
import cv2
import os

data = []
label = []
for file in os.listdir('frame/seg/1'):
    lab = [0,0,0]
    data.append(cv2.imread('frame/seg/1/' + file))
    if 'f' in file:
        lab[0] = 1
    if 'r' in file:
        lab[1] = 1
    if 'l' in file:
        lab[2] = 1
    label.append(lab)
    
data = np.array(data)
label = np.array(label)
print(data.shape)
print(label.shape)
print("Done loading")

model = Sequential()
model.add(Conv2D(128, kernel_size=3, activation='relu', input_shape=(480,640,)))
model.add(Conv2D(64, kernel_size=3, activation='relu'))
model.add(Flatten())
model.add(Dense(3, activation='softmax'))
model.summary() 

model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

model.fit(data, label, validation_split=0.1, epochs=5, batch_size=128)

_, accuracy = model.evaluate(data, label)
print('Accuracy: %f' % (accuracy*100))

model.save('model.h5')  # creates a HDF5 file 'model.h5'