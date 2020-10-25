(ns dataico.services.nick
  (:require [clojure.edn :as edn]
            [dataico.services.workbook :as dw])
  (:import (java.text SimpleDateFormat)
           (java.lang AssertionError)
           (java.util Date)
           (clojure.lang IPersistentMap)))

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

(defn date-parser
  "Converts #inst strings to java.util.Date objects

  Parameters
  + date-string : string with #inst format
                  (yyyy-MM-dd'T'HH:mm:ss.SSS)

  Returns
  Date object (java.util.Date)

  "
  [date-string]
  (.parse (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSS") date-string))

(defn pretty-date
  "Converts a java.util.Date object to a dd/MM/yyyy formatted string

  Parameters
  + dateobj : Date object (java.util.Date) or epoch time

  Returns
  dd/MM/yyyy formatted string

  "
  [dateobj]
  (->> dateobj
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
       (edn/read-string {:readers {'uuid str
                                   'inst date-parser}})))

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
   (instance? IPersistentMap data)
    (throw (AssertionError. "data is not a clojure.lang.IPersistentMap")))
  (when-not 
   (instance? IPersistentMap item)
    (throw (AssertionError. "item is not a clojure.lang.IPersistentMap")))          
  (SiigoElement.
    "" ; TODO: Set this
    (:invoice/number data)
    (get-in data [:invoice/customer :party/identification])
    "" ; TODO: Set this
    "" ; TODO: Set this
    (pretty-date (:invoice/issue-date data))
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
    (pretty-date (:invoice/payment-date data))
    "" ; TODO: Set this
    ))

(defn siigo-flatten-invoice
  "Flattens an individual invoice

  Parameters
  + invoice : An invoice (clojure.lang.PersistenHashMap)

  Returns
  A clojure.lang.LazySeq with SiigoElements inside

  "
  [invoice]
  (map #(siigo-map invoice %) (:invoice/items invoice)))

(defn dataico->siigo!
  "Creates an Excel spreadsheet by using a vector of invoices

  Parameters
  + invoices : Vector of invoices (clojure.lang.PersistenHashMap)

  Returns
  Name of the new spreadsheet

  NOTE: As side effect, creates an Excel workbook stored in the
        sheets/ directory
  "
  [invoices]
  (let [filename (str "sheets/" "out_" (.getTime (Date.)) ".xlsx")
        siigo-wb (->> (mapcat siigo-flatten-invoice invoices)
                      (map vals)
                      (dw/create-siigo-workbook!))]
    (dw/save-workbook-as! filename siigo-wb)
    (dw/resize-cols! filename)
    filename))

(dataico->siigo! invoices)
