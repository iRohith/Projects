import cv2

#flips = [2,3,4,5,6,7]

for i in range(10,15):
  cap = cv2.VideoCapture('src/'+str(i)+'.mp4')
  fourcc = cv2.VideoWriter_fourcc(*'mp4v')
  out = cv2.VideoWriter('data/'+str(i)+'.mp4',fourcc, 20.0, (640,480))
  print(i)
  while(cap.isOpened()):
      # Capture frame-by-frame
      ret, frame = cap.read()
      if frame is None:
        break
      else:
        #cv2.imshow('Img', frame)
        #cv2.waitKey(1)
        #key = chr(cv2.waitKey() & 0xFF)
        #if i in flips:
        frame = cv2.flip(frame, -1)
        #pred = pspnet.predict(frame)
        frame = cv2.resize(frame, (640, 480))
        out.write(frame)
    

  # When everything done, release the capture
  cap.release()
  out.release()
  cv2.destroyAllWindows()