(ns demo.core)

(set! (.-onload js/window)
      #(js/alert "hello wurld")) 
