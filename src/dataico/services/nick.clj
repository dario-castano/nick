(ns dataico.services.nick
  (:require
  [clojure.edn :as edn]
  [dataico.services.workbook :as dw])
  (:import (java.text SimpleDateFormat)
           (java.lang AssertionError)))

(defrecord SiigoElement [t-comprobante
                         consecutivo
                         nro-docid
                         sucursal
                         centro-costo
                         fecha-elaboracion
                         nombre-contacto
                         email-contacto
                         cod-producto
                         descr-producto
                         id-vendedor
                         bodega
                         cantidad
                         vr-unitario
                         vr-descuento
                         base-aiu
                         imp-cargo
                         imp-cargo2
                         imp-retencion
                         reteica-reteiva
                         tipo-fpago
                         vr-fpago
                         medio-pago
                         fecha-vencimiento
                         observaciones])

(defn common-date
  "Convert an #inst tagged date to a dd/MM/yyyy format

  Parameters
  + date : The date to convert (java.util.Date)

  Returns
  String with the formatted date (java.lang.String)

  "
  [date]
  (->> (prn-str date)
       (edn/read-string)
       (.format (SimpleDateFormat. "dd/MM/yyyy"))))


(defn load-invoice
  "Loads an edn invoice from the file system

  Parameters
  + filename : Path of the filename

  Returns
  edn parsed to a PersistentHashMap (clojure.lang.PersistentHashMap)

  "
  [filename]
  (->> filename
       slurp
       (edn/read-string {:readers {'uuid str}})))

(def invoice-names ["invoices/invoice1.edn"
                    "invoices/invoice2.edn"])

(def invoices (mapv load-invoice invoice-names))

(defn siigo-map
  "Creates and populates a new SiigoElement map

  Parameters
  + data : Invoice data (clojure.lang.PersistentHashMap)
  + item : One item extracted from the :invoice/items vector
           loaded by dataico.services.nick/load-invoice
           (clojure.lang.PersistentVector)

  Returns
  A SiigoElement map

  "
  [data item]
  (when-not 
   (instance? clojure.lang.IPersistentMap data)
    (throw (AssertionError. "data is not a clojure.lang.IPersistentMap")))
  (when-not 
   (instance? clojure.lang.IPersistentMap item)
    (throw (AssertionError. "item is not a clojure.lang.IPersistentMap")))          
  (SiigoElement.
    "" ; TODO: Set this
    (:invoice/number data)
    (get-in data [:invoice/customer :party/identification])
    "" ; TODO: Set this
    "" ; TODO: Set this
    (common-date (:invoice/issue-date data))
    (get-in data [:invoice/customer :party/company-name])
    (get-in data [:invoice/customer :party/email])
    (get-in item [:invoice-item/product :product/sku])
    (:invoice-item/description item)
    (get-in data [:entity/company :company/party :party/identification])
    "" ; TODO: Set this
    (:invoice-item/precise-quantity item)
    (get-in item [:invoice-item/product :product/precise-price])
    "" ; TODO: Set this
    "" ; TODO: Set this
    "" ; TODO: Set this
    "" ; TODO: Set this
    "" ; TODO: Set this
    "" ; TODO: Set this
    (:invoice/payment-means-type data)
    (:doc.analytics/total data)
    (:invoice/payment-means data)
    (common-date (:invoice/payment-date data))
    "" ; TODO: Set this
    ))

(defn siigo-flatten-invoice
  "Flatten an invoice"
  [invoice]
  (map #(siigo-map invoice %) (:invoice/items invoice)))

(defn siigo-row
  "Builds a row to insert into the spreadsheet
   
   Parameters
   + kw : a SiigoProperties keyword
   + smap : SiigoElement
   
   Returns
   Sequence with the requested data (clojure.lang.LazySeq)
   
  "
  [kw smap]
  (when-not (keyword? kw) (throw (AssertionError. "kw is not a Keyword")))
  ;(when-not (= (type smap) SiigoElement) (throw (AssertionError. "smap is not a SiigoElement")))
  (map kw (vals smap)))

;(defn dataico->siigo!
;  [invoices]
;  (let [filename (str "sheets/" "out_" (.getTime (Date.)) ".xlsx")
;        siigo-elem-seq (mapcat siigo-flatten-invoice invoices)
;        header-colors (map #(siigo-row :bgcolor %) siigo-elem-seq)
;        wb-data (conj
;                  (map #(siigo-row :value %) siigo-elem-seq)
;                  (map ))
;        siigo-wb (dw/create-siigo-workbook! siigo-seq)
;        ]
;    (dw/save-workbook-as! filename siigo-wb)
;    (dw/resize-cols! filename)
;    filename
;    ))
