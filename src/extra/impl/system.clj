(ns extra.impl.system
  (:require [clojure.core.async :as a]))

;; Constant values and protocol definitions

(def event-stream-size 1024)

(def error-size 32)

(defprotocol IExtraSystem
  (start [_])
  (stop [_])
  (register [_ process]))

;; System implementation functions

(defn start-system [state]
  state)

(defn stop-system
  "Shutdown lifecycle for a system. Should take and return the state representation."
  [state]
  (let [current-state @state
        next-state (cond-> current-state
                     ;; Eventually we'll use the registrar to shut down registered extras.
                     true (a/close! (get-in current-state :event-stream))
                     true (a/close! (get-in current-state :errors errors)))]
    (reset! state next-state)
    state))

(defrecord ExtraSystem [state]
  IExtraSystem
  (start [this] (ExtraSystem. (start-system state)))
  (stop [this] (ExtraSystem. (stop-system state)))
  (register [this process]
    ;; Currently a no-op
    (ExtraSystem. state)))


;; Public functions

(defn gen-system []
  (let [register {}
        event-stream (a/chan (a/sliding-buffer event-stream-size))
        errors (a/chan error-size)
        state (atom {:registrar registrar
                     :event-stream event-stream
                     :errors errors})]
    (ExtraSystem. state)))

