(ns llacom.quil-playground.core
  (:require [reagent.dom :as rdom]
            [reagent.core :as r]
            [quil.core :as q :include-macros true]))

;; --------------------------
;; Sample quil draw functions

(defn setup []
  (q/frame-rate 60))

(defn draw-circle [{:keys [speed]}]
  (q/background 250)
  
  (q/translate [(/ (q/width) 2) (/ (q/height) 2)])
  (let [delta (* 50 (q/sin (* speed (/ (q/millis) 1000))))]
    (q/ellipse 0 0
               (+ delta (/ (q/width) 4))
               (+ delta (/ (q/height) 4)))))

(defn draw-square [{:keys [speed]}]
  (q/background 250)
  (q/rect-mode :center)
  
  (q/translate [(/ (q/width) 2) (/ (q/height) 2)])
  (q/rotate (q/sin (* speed (/ (q/millis) 1000))))
  (q/rect 0 0 (/ (q/width) 4) (/ (q/height) 4)))

;; -------------------
;; Quil canvas element

(defn speed-range [{:keys [speed on-change]}]
  [:label {:for "speed"} (str "Speed: " speed)
   [:input {:type "range" :name "speed" :min 0 :max 10 :value speed
            :on-change on-change}]])

(defn quil-canvas [{:keys [setup draw params]}]
  (let [sketch (atom nil)
        params (r/atom {:speed 3})]
    (r/create-class
     {:display-name "quil-canvas"
      :component-will-unmount (fn [] (q/with-sketch @sketch (q/exit)))
      :component-did-mount    (fn [this]
                                (reset! sketch (q/sketch :title "playground"
                                                         :size [500 500]
                                                         :host (rdom/dom-node this)
                                                         :setup setup
                                                         :draw (fn [] (draw @params))
                                                         :features [:no-start])))
      :reagent-render (fn [] [:div
                              [speed-range
                               {:speed     (:speed @params)
                                :on-change (fn [e]
                                             (swap! params assoc :speed
                                                    (js/parseInt (.. e -target -value))))}]])})))

(def canvases
  (r/atom [{:setup setup :draw #'draw-square :params {:speed 1}}]))

(defn sketches []
  [:div
   [:h1 "Quil Sketches"]
   
   (for [[i canvas] (map-indexed vector @canvases)]
     ^{:key i} [:div {:style {:margin-bottom "50px"}}
                [quil-canvas canvas]])

   [:div.grid
    [:button {:on-click #(swap! canvases conj {:setup setup :draw #'draw-circle :params {:speed 1}})}
     "Add circle canvas"]
    [:button {:on-click #(swap! canvases conj {:setup setup :draw #'draw-square :params {:speed 1}})}
     "Add square canvas"]]])

;; -------------------------------
;; Shadow-CLJS lifecycle functions

(defn ^:dev/before-load stop []
  (js/console.log "Stopping..."))

(defn ^:dev/after-load start []
  (js/console.log "Starting...")

  (rdom/render [sketches]
               (js/document.getElementById "app")))

(defn ^:export init []
  (js/console.log "Init!")
  (start))
