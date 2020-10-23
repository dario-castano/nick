(ns dataico.services.nick-test
  (:require [clojure.test :refer :all]
            [dataico.services.nick :as nick]
            [clojure.java.io :as io]
            [clojure.data :as data]))

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
    (testing "Wrong args should fail" 
      (is (thrown? clojure.lang.ArityException 
                   (nick/load-invoice "invoice1" "invoice2"))))
    (testing "Nonexistent file should fail"
      (when (.exists (io/file nonexistent_invoice))
        (io/delete-file nonexistent_invoice)) 
      (is (thrown? java.io.FileNotFoundException 
                   (nick/load-invoice nonexistent_invoice))))
    (testing "Corrupt file should fail" 
      (is (thrown? java.lang.RuntimeException
                   (nick/load-invoice corrupt_invoice))))
    (testing "Should return a Persistent Map"
      (is (= true (instance? clojure.lang.IPersistentMap 
                             (nick/load-invoice multi_edn)))))
    (testing "Loaded EDN should return correct data"
      (is (= (:status (nick/load-invoice ok_edn)) (:status ok_map))))
    (testing "Nested EDN should return correct data"
      (is (= (data/diff (nick/load-invoice multi_edn) (multi_map)) 
             ([nil, nil, multi_map]))))
    ))