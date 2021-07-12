import cv2
import numpy as np
import os
'''
img = cv2.imread('img.jpg', cv2.IMREAD_GRAYSCALE)
img = cv2.resize(img, (640, 480))
cv2.imshow('Img', img)
cv2.waitKey()
cv2.destroyAllWindows()
'''
#road 138
acc=0
ind=1
for f in os.listdir('frame/seg/7/'):
    if chr(cv2.waitKey() & 0xFF) == 'q':
        break
    cimg = cv2.imread('frame/src/7/'+f) 
    img2 = cv2.imread('frame/seg/7/'+f)
    img = cv2.cvtColor(img2, cv2.COLOR_BGR2GRAY)
    img = np.array(img[480//3:480])
    img = (img == 138).astype(np.uint8) + (img == 219).astype(np.uint8) + (img == 113).astype(np.uint8) + (img == 57).astype(np.uint8)
    '''for i in range(0, size[0]):
        for j in range(0, size[1]):
            #img[i][j] = 1 if img[i][j] == 138 else 0
            img[i][j] = ((img[i][j] == 138) or (img[i][j] == 219) or (img[i][j] == 113) or (img[i][j] == 57))*255
    '''
    lb = img[:,0:680//3]
    fb = img[:,680//3:(2*680)//3]
    rb = img[:,(2*680)//3:680]
    pl = np.sum(lb) > (1*len(lb)*len(lb[0]))//2
    pf = np.sum(fb) > (1*len(fb)*len(fb[0]))//3
    pr = np.sum(rb) > (1*len(rb)*len(rb[0]))//2
    print(pl, pf, pr)
    acc += (('f' in f and pf) + ('r' in f and pr) + ('l' in f and pl))
    ind += 1
    #print(acc/ind)
        #cv2.imshow('frame', img)
    cv2.imshow('seg', img2)
    cv2.imshow('sec', cimg)
    cv2.imshow('img',img*255)
    cv2.imshow('lb', lb*255)
    cv2.imshow('fb', fb*255)
    cv2.imshow('rb', rb*255)
cv2.destroyAllWindows()

'''
size = img.shape
print(img[0][0])
c1 = np.array([152, 250, 152])
c2 = np.array([69, 69, 69])
c3 = np.array([69, 69, 69])
c4 = np.array([69, 69, 69])
c5 = np.array([250, 170, 29])
c6 = np.array([219, 219, 0])
c7 = np.array([106, 142, 35])
c8 = np.array([219, 19, 60])
c9 = np.array([255, 0, 0])
c10 = np.array([0, 0, 142])
c11 = np.array([0, 0, 69])
c12 = np.array([0, 60, 100])
c13 = np.array([0, 79, 100])
c14 = np.array([0, 0, 230])
c15 = np.array([119, 10, 32])
c16 = np.array([128, 64, 128])
c17 = np.array([244, 35, 231])
c18 = np.array([153, 153, 153])
for i in range(0, size[0]):
    print(i)
    for j in range(0, size[1]):
        if np.array_equal(img[i][j], c1):
            img[i][j] = c16
            print('found', 0)
        elif np.array_equal(img[i][j], c2) or np.array_equal(img[i][j], c3) or np.array_equal(img[i][j], c4):
            img[i][j] = c17
            print('found', 1)
        elif np.array_equal(img[i][j], c5) or np.array_equal(img[i][j], c6) or np.array_equal(img[i][j], c7) or np.array_equal(img[i][j], c8) or np.array_equal(img[i][j], c9) or np.array_equal(img[i][j], c10) or np.array_equal(img[i][j], c11) or np.array_equal(img[i][j], c12) or np.array_equal(img[i][j], c13) or np.array_equal(img[i][j], c14) or np.array_equal(img[i][j], c15):
            img[i][j] = c18
            print('found', 2)
        

cv2.imwrite('tes.jpg', img)
'''
