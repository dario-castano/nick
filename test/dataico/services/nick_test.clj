(ns dataico.services.nick-test
  (:require [clojure.test :refer :all]
            [dataico.services.nick :as nick]
            [clojure.java.io :as io]
            [clojure.data :as data]
            [dk.ative.docjure.spreadsheet :as xl])
  (:import (dataico.services.nick SiigoElement)
           (clojure.lang ArityException IPersistentMap)
           (java.io FileNotFoundException)
           (java.util Date)
           (java.text ParseException)))

(def siigo-element-kws '(:t-comprobante
                        :consecutivo
                        :nro-docid
                        :sucursal
                        :centro-costo
                        :fecha-elaboracion
                        :nombre-contacto
                        :email-contacto
                        :cod-producto
                        :descr-producto
                        :id-vendedor
                        :bodega
                        :cantidad
                        :vr-unitario
                        :vr-descuento
                        :base-aiu
                        :imp-cargo
                        :imp-cargo2
                        :imp-retencion
                        :reteica-reteiva
                        :tipo-fpago
                        :vr-fpago
                        :medio-pago
                        :fecha-vencimiento
                        :observaciones))

(def sample-map {:invoice/number "A0000",
                 :invoice/customer {:party/identification "123456",
                                    :party/company-name "COMPANY",
                                    :party/email "mail@mail.com"},
                 :entity/company {:company/party {:party/identification "999999"}},
                 :invoice/issue-date #inst "2020-12-24T00:00:00.000-05:00",
                 :invoice/payment-date #inst "2020-12-24T00:00:00.000-05:00",
                 :invoice/payment-means-type "1",
                 :invoice/payment-means "47",
                 :doc.analytics/total 10000,
                 :invoice/items [{:invoice-item/product {:product/sku "EM",
                                                         :product/precise-price 100},
                                  :invoice-item/description "DESCRIPTION",
                                  :invoice-item/precise-quantity 1},
                                 {:invoice-item/product {:product/sku "EM1",
                                                         :product/precise-price 100},
                                  :invoice-item/description "DESCRIPTION1",
                                  :invoice-item/precise-quantity 1},
                                 {:invoice-item/product {:product/sku "EM2",
                                                         :product/precise-price 200},
                                  :invoice-item/description "DESCRIPTION2",
                                  :invoice-item/precise-quantity 2},
                                 {:invoice-item/product {:product/sku "EM3",
                                                         :product/precise-price 300},
                                  :invoice-item/description "DESCRIPTION3",
                                  :invoice-item/precise-quantity 3}]})

(def sample-map1 {:invoice/number "A0001",
                 :invoice/customer {:party/identification "654321",
                                    :party/company-name "COMPANY1",
                                    :party/email "mail1@mail.com"},
                 :entity/company {:company/party {:party/identification "888888"}},
                 :invoice/issue-date #inst "2020-12-25T00:00:00.000-05:00",
                 :invoice/payment-date #inst "2020-12-25T00:00:00.000-05:00",
                 :invoice/payment-means-type "2",
                 :invoice/payment-means "48",
                 :doc.analytics/total 20000,
                 :invoice/items [{:invoice-item/product {:product/sku "EM-1",
                                                         :product/precise-price 400},
                                  :invoice-item/description "DESCRIPTION-1",
                                  :invoice-item/precise-quantity 4},
                                 {:invoice-item/product {:product/sku "EM1-1",
                                                         :product/precise-price 500},
                                  :invoice-item/description "DESCRIPTION1-1",
                                  :invoice-item/precise-quantity 5},
                                 {:invoice-item/product {:product/sku "EM2-1",
                                                         :product/precise-price 600},
                                  :invoice-item/description "DESCRIPTION2-1",
                                  :invoice-item/precise-quantity 6},
                                 {:invoice-item/product {:product/sku "EM3-1",
                                                         :product/precise-price 700},
                                  :invoice-item/description "DESCRIPTION3-1",
                                  :invoice-item/precise-quantity 7}]})

(def sample-map2 {:invoice/number "A0002",
                 :invoice/customer {:party/identification "987654",
                                    :party/company-name "COMPANY2",
                                    :party/email "mail2@mail.com"},
                 :entity/company {:company/party {:party/identification "777777"}},
                 :invoice/issue-date #inst "2020-12-26T00:00:00.000-05:00",
                 :invoice/payment-date #inst "2020-12-26T00:00:00.000-05:00",
                 :invoice/payment-means-type "3",
                 :invoice/payment-means "49",
                 :doc.analytics/total 30000,
                 :invoice/items [{:invoice-item/product {:product/sku "EM-2",
                                                         :product/precise-price 900},
                                  :invoice-item/description "DESCRIPTION-2",
                                  :invoice-item/precise-quantity 9},
                                 {:invoice-item/product {:product/sku "EM1-2",
                                                         :product/precise-price 800},
                                  :invoice-item/description "DESCRIPTION1-2",
                                  :invoice-item/precise-quantity 8},
                                 {:invoice-item/product {:product/sku "EM2-2",
                                                         :product/precise-price 700},
                                  :invoice-item/description "DESCRIPTION2-2",
                                  :invoice-item/precise-quantity 7},
                                 {:invoice-item/product {:product/sku "EM3-2",
                                                         :product/precise-price 600},
                                  :invoice-item/description "DESCRIPTION3-2",
                                  :invoice-item/precise-quantity 6}]})

(def ok_vals '(""
                "A0000"
                "123456"
                ""
                ""
                "24/12/2020"
                "COMPANY"
                "mail@mail.com"
                "EM"
                "DESCRIPTION"
                "999999"
                ""
                1
                100
                ""
                ""
                ""
                ""
                ""
                ""
                "1"
                10000
                "47"
                "24/12/2020"
                ""))

(def ok-map (zipmap siigo-element-kws ok_vals))

(def flattened-sample '(#dataico.services.nick.SiigoElement{:t-comprobante "",
                                                            :consecutivo "A0000",
                                                            :nro-docid "123456",
                                                            :sucursal "",
                                                            :centro-costo "",
                                                            :fecha-elaboracion "24/12/2020",
                                                            :nombre-contacto "COMPANY",
                                                            :email-contacto "mail@mail.com",
                                                            :cod-producto "EM",
                                                            :descr-producto "DESCRIPTION",
                                                            :id-vendedor "999999",
                                                            :bodega "",
                                                            :cantidad 1,
                                                            :vr-unitario 100,
                                                            :vr-descuento "",
                                                            :base-aiu "",
                                                            :imp-cargo "",
                                                            :imp-cargo2 "",
                                                            :imp-retencion "",
                                                            :reteica-reteiva "",
                                                            :tipo-fpago "1",
                                                            :vr-fpago 10000,
                                                            :medio-pago "47",
                                                            :fecha-vencimiento "24/12/2020",
                                                            :observaciones ""}
                         #dataico.services.nick.SiigoElement{:t-comprobante "",
                                                             :consecutivo "A0000",
                                                             :nro-docid "123456",
                                                             :sucursal "",
                                                             :centro-costo "",
                                                             :fecha-elaboracion "24/12/2020",
                                                             :nombre-contacto "COMPANY",
                                                             :email-contacto "mail@mail.com",
                                                             :cod-producto "EM1",
                                                             :descr-producto "DESCRIPTION1",
                                                             :id-vendedor "999999",
                                                             :bodega "",
                                                             :cantidad 1,
                                                             :vr-unitario 100,
                                                             :vr-descuento "",
                                                             :base-aiu "",
                                                             :imp-cargo "",
                                                             :imp-cargo2 "",
                                                             :imp-retencion "",
                                                             :reteica-reteiva "",
                                                             :tipo-fpago "1",
                                                             :vr-fpago 10000,
                                                             :medio-pago "47",
                                                             :fecha-vencimiento "24/12/2020",
                                                             :observaciones ""}
                         #dataico.services.nick.SiigoElement{:t-comprobante "",
                                                             :consecutivo "A0000",
                                                             :nro-docid "123456",
                                                             :sucursal "",
                                                             :centro-costo "",
                                                             :fecha-elaboracion "24/12/2020",
                                                             :nombre-contacto "COMPANY",
                                                             :email-contacto "mail@mail.com",
                                                             :cod-producto "EM2",
                                                             :descr-producto "DESCRIPTION2",
                                                             :id-vendedor "999999",
                                                             :bodega "",
                                                             :cantidad 2,
                                                             :vr-unitario 200,
                                                             :vr-descuento "",
                                                             :base-aiu "",
                                                             :imp-cargo "",
                                                             :imp-cargo2 "",
                                                             :imp-retencion "",
                                                             :reteica-reteiva "",
                                                             :tipo-fpago "1",
                                                             :vr-fpago 10000,
                                                             :medio-pago "47",
                                                             :fecha-vencimiento "24/12/2020",
                                                             :observaciones ""}
                         #dataico.services.nick.SiigoElement{:t-comprobante "",
                                                             :consecutivo "A0000",
                                                             :nro-docid "123456",
                                                             :sucursal "",
                                                             :centro-costo "",
                                                             :fecha-elaboracion "24/12/2020",
                                                             :nombre-contacto "COMPANY",
                                                             :email-contacto "mail@mail.com",
                                                             :cod-producto "EM3",
                                                             :descr-producto "DESCRIPTION3",
                                                             :id-vendedor "999999",
                                                             :bodega "",
                                                             :cantidad 3,
                                                             :vr-unitario 300,
                                                             :vr-descuento "",
                                                             :base-aiu "",
                                                             :imp-cargo "",
                                                             :imp-cargo2 "",
                                                             :imp-retencion "",
                                                             :reteica-reteiva "",
                                                             :tipo-fpago "1",
                                                             :vr-fpago 10000,
                                                             :medio-pago "47",
                                                             :fecha-vencimiento "24/12/2020",
                                                             :observaciones ""}))

(deftest date-parser-test
  (let [dateinst (Date.)
        ok_date "2020-10-25T11:34:34.044-05:00"
        obj_date "Sun Oct 25 11:34:34 COT 2020"]
    (testing "No args should fail"
      (is (thrown? ArityException
                   (nick/date-parser))))
    (testing "Wrong num of args should fail"
      (is (thrown? ArityException
                   (nick/date-parser (Date.) (Date.)))))
    (testing "Wrong type args should fail"
      (is (thrown? ClassCastException
                   (nick/date-parser 1))))
    (testing "Bad formatted string should fail"
      (is (thrown? ParseException
                   (nick/date-parser "a")))
      (is (thrown? ParseException
                   (nick/date-parser "12-12-2012")))
      (is (thrown? ParseException
                   (nick/date-parser "1999")))
      (is (thrown? ParseException
                   (nick/date-parser "1/1/1970")))
      (is (thrown? ParseException
                   (nick/date-parser "20:20")))
      (is (thrown? ParseException
                   (nick/date-parser "10.10.10")))
      (is (thrown? ParseException
                   (nick/date-parser "08:15:20.100")))
      (is (thrown? ParseException
                   (nick/date-parser "2000-01-01"))))
    (testing "Should return a java.util.Date object"
      (is (instance? Date (nick/date-parser ok_date))))
    (testing "Should parse correct formatted Dates"
      (is (= (.toString (nick/date-parser ok_date))
             obj_date)))))

(deftest pretty-date-test
  (let [sample_date #inst "1999-02-13T16:20:00.00Z"
        sample_str "13/02/1999"
        epoch 1600000000000
        epoch_str "13/09/2020"]
    (testing "No args should fail"
      (is (thrown? ArityException
                   (nick/pretty-date))))
    (testing "Wrong num args should fail"
      (is (thrown? ArityException
                   (nick/pretty-date (Date.) (Date.) (Date.)))))
    (testing "Wrong type arg should fail"
      (is (thrown? IllegalArgumentException
                   (nick/pretty-date "hello")))
      (is (thrown? IllegalArgumentException
                   (nick/pretty-date {:a 1}))))
    (testing "Should convert to the given format"
      (is (= (nick/pretty-date sample_date) sample_str))
      (is (= (nick/pretty-date epoch) epoch_str)))))

(deftest load-invoice-test
  (let [executable_file "testdata/an_executable"
        corrupt_invoice "testdata/corrupt_invoice.edn"
        nonexistent_invoice "notexists.edn"
        image "testdata/image.gif"
        ok_edn "testdata/ok.edn"
        multi_edn "testdata/multiplefields.edn"
        ok_map {:status "ok"}
        multi_map {:name "Name"
                   :address "Fake street 123"
                   :city "Cali"
                   :country "Colombia"
                   :items [{:item 1
                            :value "a"}
                           {:item 2
                            :value "b"}
                           {:item 3
                            :value "c"}
                           {:item 4
                            :value "d"}]}]
    
    (testing "No args should fail" 
      (is (thrown? ArityException
                   (nick/load-invoice))))
    (testing "Wrong number of args should fail" 
      (is (thrown? ArityException
                   (nick/load-invoice "invoice1" "invoice2"))))
    (testing "Nonexistent file should fail"
      (when (.exists (io/file nonexistent_invoice))
        (io/delete-file nonexistent_invoice)) 
      (is (thrown? FileNotFoundException
                   (nick/load-invoice nonexistent_invoice))))
    (testing "Wrong kind of file should fail" 
      (is (thrown? RuntimeException
                   (nick/load-invoice executable_file))))
    (testing "Corrupt file should fail" 
      (is (thrown? RuntimeException
                   (nick/load-invoice corrupt_invoice))))
    (testing "Should return a Persistent Map"
      (is (= true (instance? IPersistentMap
                             (nick/load-invoice multi_edn)))))
    (testing "Loaded EDN should return correct data"
      (is (= (:status (nick/load-invoice ok_edn)) (:status ok_map))))
    (testing "Nested EDN should return correct data"
      (is (= (data/diff (nick/load-invoice multi_edn) multi_map) 
             [nil, nil, multi_map])))
    ))

(deftest siigo-map-test
  (let [sample_date #inst "1985-04-12T23:20:50.52Z"
        sample_uuid #uuid "c1197d5a-01e7-47b5-8697-69d5d906a69f"
        invoice-item (first (:invoice/items sample-map))
        siigo-elem-kwcount (count siigo-element-kws)]
    
    (testing "No args should fail" 
      (is (thrown? ArityException
                   (nick/siigo-map))))
    (testing "Wrong number of args should fail" 
      (is (thrown? ArityException
                   (nick/siigo-map "a")))
      (is (thrown? ArityException
                   (nick/siigo-map 1 2 3))))
    (testing "Wrong type args should fail" 
      (is (thrown? AssertionError
                   (nick/siigo-map sample_date 0)))
      (is (thrown? AssertionError
                   (nick/siigo-map "a" sample_uuid)))
      (is (thrown? AssertionError
                   (nick/siigo-map sample-map 0)))
      (is (thrown? AssertionError
                   (nick/siigo-map 0 (:invoice/items sample-map)))))
    (testing "Correct args should return a SiigoElement" 
      (is (= 
           (type (nick/siigo-map sample-map invoice-item))
           SiigoElement)))
    (testing "SiigoElement should have right num of keywords" 
      (is (= siigo-elem-kwcount 
             (-> (nick/siigo-map sample-map invoice-item)
                 keys
                 count))))
    (testing "SiigoElement should have the correct keywords" 
      (is (= 
           (keys (nick/siigo-map sample-map invoice-item)) 
           siigo-element-kws)))
    (testing "Should be populating correct values"
      (let [testdata (nick/siigo-map sample-map invoice-item)]
        (is (= (data/diff testdata ok-map) [nil, nil, ok-map]))
        ))
   ))

(deftest siigo-flatten-invoice-test
  (testing "Should flatten the right sequence"
    (is (= (nick/siigo-flatten-invoice sample-map) flattened-sample))))

(deftest dataico->siigo!-test
  (let [invoices [sample-map sample-map1 sample-map2]
        values (->> invoices
                    (map nick/siigo-flatten-invoice)
                    (map vals))
        name (nick/dataico->siigo! invoices)
        rows (->> (xl/load-workbook-from-file name)
                  (xl/select-sheet "Hoja1")
                  xl/row-seq)]
    (testing "Should write correct data"
      (is (= rows values))
      )))
