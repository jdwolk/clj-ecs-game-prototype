(ns ecs-test.engine.time
  (:import (java.util.concurrent TimeUnit)))

; convert nanoseconds -> seconds
; (.convert (TimeUnit/SECONDS) (- end start) (TimeUnit/NANOSECONDS))

(comment
(defprotocol IClock
  (get-cps [this]) ; Gets cycles-per-seoncd
  (get-cycles [this]) ; Returns cycles passed since clock started
  (delta-seconds [this clock]) ; Give delta in cycles between this and other
  (to-cycles [this seconds])
  (to-seconds [this cycles])
  (update [this dt-seconds]))

(defrecord Clock [cps cycles]
  IClock
  (get-cps [this] cps)
  (get-cycles [this] cycles)
  (delta-seconds [this clock]
    (to-seconds (- (get-cycles this) 
                   (get-cycles clock))))
  (to-cycles [this seconds]
    (* seconds (get-cps this)))
  (to-seconds [this cycles]
    (/ cycles (get-cps this)))
  (update [this dt-seconds]
    (->Clock cps (to-cycles this
                   (+ dt-seconds 
                      (to-seconds this (get-cycles this)))))))

(defn make-clock [cps]
  (->Clock cps 0)) ; Start clock w/ 0 cycles


(def nanos-in-milli (.convert (TimeUnit/NANOSECONDS) 1 
                              (TimeUnit/MILLISECONDS)))

(defn readCPS []
  "Reads the System's number of cycles per second
   # of nanoseconds (i.e. cycles) after resting for 1 millisecond.
   Need to convert that value from milliseconds -> seconds"
  (for [_ (range 10)]
    (let [start (System/nanoTime)
          _ (Thread/sleep 1) ; sleep for 1 milli
          delta (- (System/nanoTime) start)] 
      ; delta is actual nanos elapsed in 1 ms
      (println "DELTA: " delta)
      (* 1000 delta) ;actual nanos in virtual second
      )))

) ;end comment

;1000000000 nanos in 1 sec
;1 frame/dnanos * nanos / sec = frame / sec
(defn calc-fps [start end]
  (let [dnanos (- end start)
        npsec 1000000000]
    (int (* (/ 1 dnanos) npsec))))

