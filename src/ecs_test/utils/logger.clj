(ns ecs-test.utils.logger 
  (:refer-clojure :rename {log core-log})
  (:use [clj-logging-config.log4j :exclude [log]]
        [clojure.tools.logging :rename {log tools-log}]))

(def ^:dynamic *verbosity* 2)
(def log-agent (agent 0))

(def debug-lvls  (zipmap [:debug1 :debug2 :debug3]
                        (repeatedly (constantly :debug))))
(def normal-lvls (apply zipmap ((juxt identity identity)
                               [:trace :info :warn :error :debug])))
(def lookup-lvl (merge debug-lvls normal-lvls))


(set-logger!
  :pattern "%-6p%m%n"
  :level :debug)

;(def log-config {:pattern "%-6p%m%n" :level :debug})
;(set-loggers!
;  "ecs-test.stdout" log-config
;  "ecs-test.file" (merge log-config {:out "out.log"}))

(defn verbose-enough?
  "Meant for determining if debugN levels match the verbosity.
  ONLY PASS IN :debugN levels!"
  [debug-lvl]
  (<= (int (read-string (str (last (name debug-lvl))))) ;XXX this sux
      *verbosity*))

(defn should-log? [lvl]
  (boolean (or (lvl (set (keys normal-lvls)))     ; if it's a normal lvl or
               (and (lvl (set (keys debug-lvls))) ; if it's a debug
                    (verbose-enough? lvl)))))     ; and verbose enough
  
(defn do-log [msg-id lvl msg]
  (if (should-log? lvl) 
      (do (tools-log (lookup-lvl lvl) msg)
          (inc msg-id)))
      msg-id) ;Don't increment if no logging happened

(defn log [lvl channel & msg]
  (send-off log-agent do-log lvl 
            (format "%-12s %s" (str "[" (name channel) "]") (apply str msg))))

