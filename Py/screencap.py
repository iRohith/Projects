'''
Capture screen helper
'''
import mss, numpy, threading, sys, select, traceback

class screencap:
    def __init__(self, msgLooper=False, top=0, left=0, width=100, height=100):
        self.__msgLooper = msgLooper
        self.__msg = None
        self.__bounding = {"top": top, "left": left, "width": width, "height": height}
        self.__initX = 0
        self.__initY = 0
        self.running = True
        self.__sct = None
        self.__cmdThread = None

    def __enter__(self):
        self.start()
        return self

    def __exit__(self, exc_type, exc_value, tb):
        self.release()
        if exc_type is not None:
            traceback.print_exception(exc_type, exc_value, tb)
        return True

    def __call__(self):
        return self.capture()
    
    def start(self):
        self.__sct = mss.mss()
        self.__cmdThread = threading.Thread(target=self.__cmdInputRoutine) if self.__msgLooper else None
        if self.__msgLooper:
            self.__cmdThread.start()

    def release(self):
        self.__msg = 'quit'
        self.running = False
        self.__sct.close()
        if self.__msgLooper and self.__cmdThread.is_alive():
            self.__cmdThread.join()

    def capture(self):
        return numpy.array(self.__sct.grab(self.__bounding))

    def set_coords(self, key):
        if key == ord('p'):
            (x, y) = autopy.mouse.location()
            self.__initX = int(x)
            self.__initY = int(y)
        elif key == ord('s'):
            (x, y) = autopy.mouse.location()
            x = int(x)
            y = int(y)
            w = x - self.__initX
            h = y - self.__initY
            if w == 0 or h == 0:
                return
            if w < 0:
                w = -w
            else : x = self.__initX
            if h < 0:
                h = -h
            else : y = self.__initY
            self.__bounding = {"top": y, "left": x, "width": w, "height": h}

    def __cmdInputRoutine(self):
        while self.__msg != 'quit':
            read = self.input_timeout()
            if read is not None:
                self.__msg = read
                if self.__msg == 'position' or self.__msg == 'p':
                    self.set_coords(ord('p'))
                elif self.__msg == 'size' or self.__msg == 's':
                    self.set_coords(ord('s'))
                elif self.__msg == 'quit' or self.__msg == 'q':
                    self.running = False
                    self.__sct.close()

    @staticmethod
    def input_timeout(timeout=0.1):
        """
        Read input with a timeout
        """
        return sys.stdin.readline().strip() if select.select( [sys.stdin], [], [], timeout)[0] else None
