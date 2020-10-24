(ns dataico.services.nick
  (:require
  [clojure.edn :as edn])
;   [dataico.services.workbook :as dw])
  (:import (java.text SimpleDateFormat)))

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
  (str (.format (SimpleDateFormat. "dd/MM/yyyy") date)))


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
                                           'inst str}})))

(def invoice-names ["invoices/invoice1.edn"
                    "invoices/invoice2.edn"])

(def invoices (mapv load-invoice invoice-names))

(defn siigo-map
  "Creates and populates a new SiigoElement map

  Parameters
  + data : Invoice data (clojure.lang.PersistentHashMap)
  + item : Each item extracted from the :invoice/items vector
           loaded by dataico.services.nick/load-invoice
           (clojure.lang.PersistentVector)

  Returns
  A SiigoElement map with SiigoProperties elements inside

  "
  [data item]
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
    (:invoice/issue-date data)
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
    (:invoice/payment-date data)
    :blue-bg)
   (SiigoProperties.
    "Observaciones"
    "" ; TODO: Set this
    :blue-bg)))

(defn siigo-row
  "Builds a row to insert into the spreadsheet
   
   Parameters
   + kw : a SiigoProperties keyword
   + siigomap : SiigoElement
   
   Returns
   Sequence with the requested data (clojure.lang.LazySeq)
   
  "
  [kw siigomap]
  (map kw (vals siigomap)))