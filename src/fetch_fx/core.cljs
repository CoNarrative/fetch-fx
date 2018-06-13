(ns fetch-fx.core)
  (:require [cljs.spec.alpha :as s]
            [re-frame.core :as rf])


(s/def ::url string?)
(s/def ::method string?)
(s/def ::on-success vector?)
(s/def ::on-failure vector?)
(s/def ::response-format #{:edn :text :raw})
(s/def ::request-body-format #{:json})
(s/def ::headers (s/map-of string? any?))
(s/def ::body (s/or :form-data #(instance? js/FormData %)
                    :map map?
                    :string string?))

(s/def ::request
  (s/keys :req-un [::url ::method ::on-success ::on-failure ::response-format]
          :opt-un [::headers ::body]))


(defn get-headers [headers]
  (let [data (atom {})]
    (loop [entries (.entries headers)]
      (when-let [entry (aget (.next entries) "value")]
        (swap! data assoc (keyword (aget entry 0)) (aget entry 1))
        (recur entries)))
    @data))

(defn get-response-data [response]
  (let [headers (get-headers (aget response "headers"))]
    {:headers        headers
     :redirected     (aget response "redirected")
     :ok             (aget response "ok")
     :status         (aget response "status")
     :status-text    (aget response "statusText")
     :type           (aget response "type")
     :url            (aget response "url")
     :use-final-url? (aget response "useFinalUrl")
     :body           (aget response "body")
     :body-used?     (aget response "bodyUsed")}))

(defn get-js-request-options [{:keys [method headers body request-body-format]
                               :or   {method "GET"}}]
  (clj->js
    (merge (when method {:method method})
           (when headers {:headers (if (= request-body-format :json)
                                     (merge headers {:Content-Type "application/json"})
                                     headers)})
           (when body {:body (case request-body-format
                               :json (js/JSON.stringify (clj->js body))
                               (js/JSON.stringify (clj->js body)))}))))


(defn fetch [{:keys [url method on-success on-failure response-body-format return-response? headers body
                     request-body-format]
              :or   {return-response?    false
                     request-body-format :json}
              :as   args}]
  (let [res (atom nil)]
    (-> (js/fetch url (get-js-request-options args))
        (.then (fn [response]
                 (when return-response? (reset! res response))
                 (case response-body-format
                   :edn (.json response)
                   :text (.text response)
                   (.resolve js/Promise (.-body response)))))
        (.then (fn [body]
                 (let [formatted-body (case response-body-format
                                        :edn (js->clj body :keywordize-keys true)
                                        body)]
                   (rf/dispatch (conj on-success
                                      (if return-response?
                                        (assoc (get-response-data @res)
                                          :body formatted-body)
                                        formatted-body))))))
        (.catch (fn [err]
                  (rf/dispatch (conj on-failure err)))))))

(defn fetch-effect
  [request]
  (let [request-maps (if (sequential? request) request [request])]
    (doseq [request request-maps] (fetch request))))

(rf/reg-fx :fetch fetch-effect)

