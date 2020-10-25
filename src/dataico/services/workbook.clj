(ns dataico.services.workbook
  (:require [dk.ative.docjure.spreadsheet :as xl])
  (:import (java.io FileInputStream FileOutputStream)
           (org.apache.poi.xssf.usermodel XSSFWorkbookFactory)))

(def header-style [{:colname "Tipo de comprobante", :bgcolor :red-bg}
                   {:colname "Consecutivo", :bgcolor :red-bg}
                   {:colname "CC / Nit Cliente", :bgcolor :red-bg}
                   {:colname "Sucursal", :bgcolor :blue-bg}
                   {:colname "Centro de costo", :bgcolor :blue-bg}
                   {:colname "Fecha de elaboración", :bgcolor :red-bg}
                   {:colname "Nombre contacto", :bgcolor :blue-bg}
                   {:colname "Email contacto", :bgcolor :blue-bg}
                   {:colname "Código producto", :bgcolor :red-bg}
                   {:colname "Descripción producto", :bgcolor :blue-bg}
                   {:colname "Identificación vendedor", :bgcolor :red-bg}
                   {:colname "Bodega", :bgcolor :blue-bg}
                   {:colname "Cantidad", :bgcolor :red-bg}
                   {:colname "Valor unitario", :bgcolor :red-bg}
                   {:colname "Valor Descuento", :bgcolor :blue-bg}
                   {:colname "Base AIU", :bgcolor :blue-bg}
                   {:colname "Impuesto Cargo", :bgcolor :blue-bg}
                   {:colname "Impto. Cargo 2", :bgcolor :blue-bg}
                   {:colname "Impuesto Retencion", :bgcolor :blue-bg}
                   {:colname "ReteICA - ReteIVA", :bgcolor :blue-bg}
                   {:colname "Tipo de Forma de Pago", :bgcolor :red-bg}
                   {:colname "Valor de Forma de Pago", :bgcolor :red-bg}
                   {:colname "Medio de Pago", :bgcolor :blue-bg}
                   {:colname "Fecha Vencimiento", :bgcolor :blue-bg}
                   {:colname "Observaciones", :bgcolor :blue-bg}])

(defn create-siigo-workbook!
  "Creates a siigo-like workbook using docjure

  Parameters
  + siigo-invoices : Sequence with sequences (dataico.services.nick/siigo-ordered-seq)

  Returns
  XSSFWorkbook (org.apache.poi.xssf.usermodel.XSSFWorkbook)

  "
  [siigo-invoices]
  (let [wb (xl/create-workbook "Hoja1" [(map :colname header-style)])
        sheet (xl/select-sheet "Hoja1" wb)
        header-row (first (xl/row-seq sheet))
        header-cells (xl/cell-seq header-row)
        white-font (xl/create-font! wb {
                                        :name  "Calibri",
                                        :size  12,
                                        :color :white,
                                        })
        blue-cell (xl/create-cell-style! wb {
                                             :halign      :center,
                                             :background  :blue,
                                             :font        white-font,
                                             :wrap        true
                                             })
        red-cell (xl/create-cell-style! wb {
                                            :halign      :center,
                                            :background  :red,
                                            :font        white-font,
                                            :wrap        true
                                            })
        apply-style (fn [style]
                      (cond
                        (= (:bgcolor style) :red-bg) red-cell
                        (= (:bgcolor style) :blue-bg) blue-cell))
        stylelist (map apply-style header-style)
        set-styles! (fn [cells styles]
                      (dorun (map xl/set-cell-style! cells styles)))]

    (set-styles! header-cells stylelist)
    (xl/add-rows! sheet siigo-invoices)
    wb))

(defn open-xlsx
  "Loads a XSSFWorkbook from a xlsx file

  Parameters
  + filename : Path of the xlsx book

  Returns
  Docjure workbook (XSSFWorkbook)
  "
  [filename]
  (->> filename
       (new FileInputStream)
       (. XSSFWorkbookFactory createWorkbook)))

(defn show-wb-content
  "Gets a sequence of rows from a XSSFWorkbook

  Parameters
  + workbook : Docjure workbook (XSSFWorkbook)
  + sheet-idx : Index of the sheet you want to get
                the data

  Returns
  A sequence (clojure.lang.LazySeq) with a list by each
  row of the sheet
  "
  [workbook sheet-idx]
  (->> (. workbook getSheetAt sheet-idx)
       (xl/row-seq)
       (map #(iterator-seq (.iterator %)))
       (map (fn [x] (map (fn [y] (.toString y)) x)))))

(defn resize-cols!
  "Autoresize columns of a SIIGO spreadsheet (25 cols)

  Parameters
  + filename : Path of the SIIGO-formatted XLSX file

  Returns
  nil

  "
  [filename]
  (let [file-to-resize (new FileInputStream filename)
        workbook (. XSSFWorkbookFactory createWorkbook file-to-resize)
        sheet (. workbook getSheetAt 0)
        out (new FileOutputStream filename)
        max-cols 25]

    (doseq [x (range max-cols)] (. sheet autoSizeColumn x))
    (. workbook write out)
    (. out close)))

(defn save-workbook-as!
  "Saves a workbook into a XLSX spreadsheet

  Parameters
  + filename : Path of the XLSX spreadsheet
  + workbook : XSSFWorkbook (org.apache.poi.xssf.usermodel.XSSFWorkbook)

  Returns
  nil

  "
  [filename workbook]
  (xl/save-workbook! filename workbook))
