
(use '[leiningen.exec :only (deps)])
(deps '[[clj-http "1.1.2"]
      [enlive "1.1.5"]])

(require '[clj-http.client :as client]
         '[net.cgrand.enlive-html :as html])



(def numberof-urls-crawled  (ref 0))
(def crawled-urls (ref #{}))

(defn relative-url? [s]
  (not (re-find #"https?://" (str s))))


(defn crawl
  [seed-site-url]
  (let [html (html/html-resource (do
                                   (println (str "crawling " seed-site-url))
                                   (java.net.URL. seed-site-url)))
        anchor-tags (html/select html [:a])
        urls-to-crawl (set (remove (fn [s] (or (empty? s) (relative-url? s))) (map (fn [m] (get-in m [:attrs :href])) anchor-tags)))
        ]
    (do
      (dosync
       (alter numberof-urls-crawled + (count urls-to-crawl))
       (alter crawled-urls conj seed-site-url))
      (if (not (empty? urls-to-crawl)) (doall (map crawl (clojure.set/difference urls-to-crawl @crawled-urls)))))))


(crawl (second *command-line-args*))
