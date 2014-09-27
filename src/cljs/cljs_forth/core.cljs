(ns cljs-forth.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [figwheel.client :as figwheel :include-macros true]
            [weasel.repl :as weasel]
            [sablono.core :as html :refer-macros [html]]
            [cljs-forth.interpreter :as inter]))

(defonce app-state (atom {:state {:stack [] :vocab inter/default-vocab
                                  :console []}}))

(defn handle-change [e owner {:keys [text]}]
  (om/set-state! owner :text (.. e -target -value)))

(defn handle-enter [e app owner {:keys [text]}]
  (when (= 13 (.-keyCode e))
    (om/transact! app [:state] #(inter/exec-line % text))
    (om/set-state! owner :text "")))

(defn input-field [app owner]
  (reify
    om/IInitState
    (init-state [_] {:text ""})
    om/IRenderState
    (render-state [this state]
      (html [:div.repl [:input {:value (:text state)
                           :onChange #(handle-change % owner state)
                           :onKeyUp #(handle-enter % app owner state)}]]))))

(om/root
  (fn [app owner]
    (om/component
     (html [:div
            [:pre.console (apply str (get-in app [:state :console]))]
            [:div.stack "Stack: " (clojure.string/join " " (-> app :state :stack))]
            (om/build input-field app)])))
  app-state
  {:target (. js/document (getElementById "app"))})

(def is-dev (.contains (.. js/document -body -classList) "is-dev"))

(when is-dev
  (enable-console-print!)
  (figwheel/watch-and-reload
   :websocket-url "ws://localhost:3449/figwheel-ws"
   :jsload-callback (fn [] (print "reloaded")))
  (weasel/connect "ws://localhost:9001" :verbose true))
