import cv2

flips = [2,3,4,5,6,7]

for i in range(1,10):
  cap = cv2.VideoCapture('/home/rohith/Desktop/IDP/Data/'+str(i)+'.mp4')
  fourcc = cv2.VideoWriter_fourcc(*'mp4v')
  out = cv2.VideoWriter('/home/rohith/Desktop/IDP/Data/bsrc/'+str(i)+'.mp4',fourcc, 20.0, (640,480))
  print(i)
  while(cap.isOpened()):
      # Capture frame-by-frame
      ret, frame = cap.read()
      if frame is None:
        break
      else:
        if i in flips:
          frame = cv2.flip(frame, -1)
        #pred = pspnet.predict(frame)
        #pred = cv2.resize(pred, (640, 480))
        out.write(frame)
    

  # When everything done, release the capture
  cap.release()
  out.release()