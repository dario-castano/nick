(ns dataico.services.nick-test
  (:require [clojure.test :refer :all]
            [dataico.services.nick :as nick]
            [clojure.java.io :as io]
            [clojure.data :as data])
  (:import (dataico.services.nick SiigoElement SiigoProperties)))

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

(def siigo-property-kws '(:title :value :bgcolor))

(def sample-map {:invoice/number "A0000",
                 :invoice/customer {:party/identification "123456",
                                    :party/company-name "COMPANY",
                                    :party/email "mail@mail.com"},
                 :entity/company {:company/party {:party/identification "999999"}},
                 :invoice/issue-date #inst "2020-12-12",
                 :invoice/payment-date #inst "2020-12-12"
                 :invoice/payment-means-type "1",
                 :invoice/payment-means "47",
                 :doc.analytics/total 10000,
                 :invoice/items [{:invoice-item/product {:product/sku "EM",
                                                         :product/precise-price 100},
                                  :invoice-item/description "DESCRIPTION",
                                  :invoice-item/precise-quantity 1}]
                 })
(def ok_vals '(""
                "A0000"
                "123456"
                ""
                ""
                "11/12/2020"
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
                "11/12/2020"
                ""))

(def ok-map (zipmap siigo-element-kws ok_vals))

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
      (is (thrown? clojure.lang.ArityException 
                   (nick/load-invoice))))
    (testing "Wrong number of args should fail" 
      (is (thrown? clojure.lang.ArityException 
                   (nick/load-invoice "invoice1" "invoice2"))))
    (testing "Nonexistent file should fail"
      (when (.exists (io/file nonexistent_invoice))
        (io/delete-file nonexistent_invoice)) 
      (is (thrown? java.io.FileNotFoundException 
                   (nick/load-invoice nonexistent_invoice))))
    (testing "Wrong kind of file should fail" 
      (is (thrown? java.lang.RuntimeException 
                   (nick/load-invoice executable_file))))
    (testing "Corrupt file should fail" 
      (is (thrown? java.lang.RuntimeException
                   (nick/load-invoice corrupt_invoice))))
    (testing "Should return a Persistent Map"
      (is (= true (instance? clojure.lang.IPersistentMap 
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
      (is (thrown? clojure.lang.ArityException 
                   (nick/siigo-map))))
    (testing "Wrong number of args should fail" 
      (is (thrown? clojure.lang.ArityException 
                   (nick/siigo-map "a")))
      (is (thrown? clojure.lang.ArityException
                   (nick/siigo-map 1 2 3))))
    (testing "Wrong type args should fail" 
      (is (thrown? java.lang.AssertionError 
                   (nick/siigo-map sample_date 0)))
      (is (thrown? java.lang.AssertionError
                   (nick/siigo-map "a" sample_uuid)))
      (is (thrown? java.lang.AssertionError
                   (nick/siigo-map sample-map 0)))
      (is (thrown? java.lang.AssertionError
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
    (testing "Each element in SiigoElement should be a SiigoProperty"
      (is (every? #(= % SiigoProperties)
                  (map type (vals
                              (nick/siigo-map sample-map invoice-item))))))
    (testing "Nested SiigoProperties should have right num of keywords"
      (is (every? #(= % (count siigo-property-kws))
                  (->> (vals (nick/siigo-map sample-map invoice-item))
                       (map keys)
                       (map count)))))
    (testing "Should be populating correct values"
      (let [smap (nick/siigo-map sample-map invoice-item)
            testdata (zipmap (keys smap) (map :value (vals smap)))]
        (is (= (data/diff testdata ok-map) [nil, nil, ok-map]))
        ))
   ))

(deftest siigo-row-test)