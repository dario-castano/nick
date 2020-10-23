(ns dataico.services.nick
  (:require
   ;[dataico.services.workbook :as dw]
   [clojure.spec.alpha :as s])
  (:import (java.util Date)
           (java.text SimpleDateFormat)))

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
       (clojure.edn/read-string {:readers {'uuid str
                                           'inst str}})))

(def invoice-names ["invoices/invoice1.edn"
                    "invoices/invoice2.edn"])

(def invoices (mapv load-invoice invoice-names))