(ns dataico.services.nick
  (:require
  [clojure.edn :as edn])
;   [dataico.services.workbook :as dw])
  (:import (java.text SimpleDateFormat)
           (java.lang AssertionError)))

(defrecord SiigoProperties [title value bgcolor])

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
  A SiigoElement map with SiigoProperties elements inside

  "
  [data item]
  (when-not 
   (instance? clojure.lang.IPersistentMap data)
    (throw (AssertionError. "data is not a clojure.lang.IPersistentMap")))
  (when-not 
   (instance? clojure.lang.IPersistentMap item)
    (throw (AssertionError. "item is not a clojure.lang.IPersistentMap")))          
  (SiigoElement.
   (SiigoProperties.
    "Tipo de comprobante"
    "" ; TODO: Set this
    :red-bg)
   (SiigoProperties.
    "Consecutivo"
    (:invoice/number data)
    :red-bg)
   (SiigoProperties.
    "CC / Nit Cliente"
    (get-in data [:invoice/customer :party/identification])
    :red-bg)
   (SiigoProperties.
    "Sucursal"
    "" ; TODO: Set this
    :blue-bg)
   (SiigoProperties.
    "Centro de costo"
    "" ; TODO: Set this
    :blue-bg)
   (SiigoProperties.
    "Fecha de elaboración"
    (common-date (:invoice/issue-date data))
    :red-bg)
   (SiigoProperties.
    "Nombre contacto"
    (get-in data [:invoice/customer :party/company-name])
    :blue-bg)
   (SiigoProperties.
    "Email contacto"
    (get-in data [:invoice/customer :party/email])
    :blue-bg)
   (SiigoProperties.
    "Código producto"
    (get-in item [:invoice-item/product :product/sku])
    :red-bg)
   (SiigoProperties.
    "Descripción producto"
    (:invoice-item/description item)
    :blue-bg)
   (SiigoProperties.
    "Identificación vendedor"
    (get-in data [:entity/company :company/party :party/identification])
    :red-bg)
   (SiigoProperties.
    "Bodega"
    "" ; TODO: Set this
    :blue-bg)
   (SiigoProperties.
    "Cantidad"
    (:invoice-item/precise-quantity item)
    :red-bg)
   (SiigoProperties.
    "Valor unitario"
    (get-in item [:invoice-item/product :product/precise-price])
    :red-bg)
   (SiigoProperties.
    "Valor Descuento"
    "" ; TODO: Set this
    :blue-bg)
   (SiigoProperties.
    "Base AIU"
    "" ; TODO: Set this
    :blue-bg)
   (SiigoProperties.
    "Impuesto Cargo"
    "" ; TODO: Set this
    :blue-bg)
   (SiigoProperties.
    "Impto. Cargo 2"
    "" ; TODO: Set this
    :blue-bg)
   (SiigoProperties.
    "Impuesto Retencion"
    "" ; TODO: Set this
    :blue-bg)
   (SiigoProperties.
    "ReteICA - ReteIVA"
    "" ; TODO: Set this
    :blue-bg)
   (SiigoProperties.
    "Tipo de Forma de Pago"
    (:invoice/payment-means-type data)
    :red-bg)
   (SiigoProperties.
    "Valor de Forma de Pago"
    (:doc.analytics/total data)
    :red-bg)
   (SiigoProperties.
    "Medio de Pago"
    (:invoice/payment-means data)
    :blue-bg)
   (SiigoProperties.
    "Fecha Vencimiento"
    (common-date (:invoice/payment-date data))
    :blue-bg)
   (SiigoProperties.
    "Observaciones"
    "" ; TODO: Set this
    :blue-bg)))

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
