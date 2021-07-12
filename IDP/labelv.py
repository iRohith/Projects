import cv2
import os

for i in range(14, 15):
    capSrc = cv2.VideoCapture('data/'+str(i)+'.mp4')
    capSeg = cv2.VideoCapture('tran/seg/'+str(i)+'.mp4')
    capDep = cv2.VideoCapture('tran/dep/'+str(i)+'.mp4')
    print(i)
    ind = 1
    while(capSrc.isOpened()):
        _, src = capSrc.read()
        _, seg = capSeg.read()
        _, dep = capDep.read()
        name = ''
        if src is None:
            break
        else:
            cv2.imshow('Img', src)
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
                    cv2.imwrite('frame/src/'+str(i)+'/'+str(ind)+name+'.jpg', src)
                    cv2.imwrite('frame/seg/'+str(i)+'/'+str(ind)+name+'.jpg', seg)
                    cv2.imwrite('frame/dep/'+str(i)+'/'+str(ind)+name+'.jpg', dep)
                    ind += 1
                continue
            break
    else:
        continue
    break

capSrc.release()
capSeg.release()
capDep.release()
cv2.destroyAllWindows()