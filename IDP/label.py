import cv2
import os
from shutil import copyfile

for i in range(1, 10):
    ind=1
    for filename in os.listdir('frames/src/' + str(i)):
        name = ''
        img = cv2.imread(os.path.join('frames/src/'+str(i), filename))
        cv2.imshow('image', img)
        key = 'z'
        while key != 'n':
            key = chr(cv2.waitKey() & 0xFF)
            if key == 'n':
                pass
            elif key == 'q':
                break
            elif key == 'c':
                if len(name) > 0:
                    name = name[:-1]
            elif key == 'f' or key == 'l' or key == 'r':
                if key not in name:
                    name += key
        else:
            if len(name)>0:
                copyfile(os.path.join('frames/src/'+str(i), filename), os.path.join('label/src/'+str(i), str(ind)+name+'.jpg'))
                copyfile(os.path.join('frames/dep/'+str(i), filename), os.path.join('label/dep/'+str(i), str(ind)+name+'.jpg'))
                copyfile(os.path.join('frames/seg/'+str(i), filename), os.path.join('label/seg/'+str(i), str(ind)+name+'.jpg'))
                ind += 1
            continue
        break
    else:
        continue
    break
cv2.destroyAllWindows()