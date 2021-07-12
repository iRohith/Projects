import cv2

flips = [2,3,4,5,6,7]

for i in range(1,10):
  cap = cv2.VideoCapture('/home/rohith/Desktop/IDP/Transform/Seg/'+str(i)+'.mp4')
  j=0
  print(i)
  while(cap.isOpened()):
      # Capture frame-by-frame
      ret, frame = cap.read()
      if frame is None:
        break
      else:
        cv2.imwrite('/home/rohith/Desktop/IDP/frames/seg/'+str(i)+'/'+str(j)+'.jpg', frame)
        j+=1
        #pred = pspnet.predict(frame)
        #pred = cv2.resize(pred, (640, 480))
        #out.write(frame)
    

  # When everything done, release the capture
  cap.release()
  #out.release()