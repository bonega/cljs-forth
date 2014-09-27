(ns cljs-forth.interpreter)

(def console-height 16)

(defn forth-print [state s]
  (update-in state [:console]
             (comp vec (partial take-last console-height) #(conj % (str s "\n")))))

(defn stack-error [state]
  (forth-print state "Stack error"))

(defn minus [state]
  (let [[a b] (take-last 2 (:stack state))
        state (update-in state [:stack] (comp vec (partial drop-last 2)))]
    (if (and a b)
      (update-in state [:stack] #(conj % (- a b)))
      (stack-error state))))

(defn swap [state]
  (let [[a b] (take-last 2 (:stack state))
        state (update-in state [:stack] (comp vec (partial drop-last 2)))]
    (if (and a b)
      (update-in state [:stack] #(conj % b a))
      (stack-error state))))

(defn drop [state]
  (if (seq (:stack state))
    (update-in state [:stack] (comp vec butlast))
    (stack-error state)))

(defn dot [state]
  (if-let [v (last (:stack state))]
    (forth-print (update-in state [:stack] (comp vec butlast)) v)
    (stack-error state)))

(def default-vocab {"-" minus "swap" swap "drop" drop "." dot})

(defn exec [state word]
  (let [word-fn (get-in state [:vocab word])]
    (if word-fn
      (word-fn state)
      (let [num (js/parseInt word)]
        (if (integer? num)
          (update-in state [:stack] #(conj % num))
          (forth-print state (str "Word " word " doesn't exist in vocabulary")))))))

(defn exec-line [state line]
  (let [words (clojure.string/split (clojure.string/trim line) #"\s+")]
    (reduce exec state words)))
