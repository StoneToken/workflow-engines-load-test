/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acme.kogitobpmn;

public class Payment {

    private Integer id;
    private Integer sum;
    private String account;
    private String description;
    private String date;
    private Integer prepareResult;
    private Integer paymentResult;

    public Payment() {
    }

    public Payment(Integer id, Integer sum, String account, String description, String date, Integer prepareResult,
            Integer paymentResult) {
        super();
        this.id = id;
        this.sum = sum;
        this.account = account;
        this.description = description;
        this.date = date;
        this.prepareResult = prepareResult;
        this.paymentResult = paymentResult;
    }

    public Integer getId() {
        return id;
    }

    public Integer getPrepareResult() {
        return prepareResult;
    }

    public void setPrepareResult(Integer prepareResult) {
        this.prepareResult = prepareResult;
    }

    public Integer getPaymentResult() {
        return paymentResult;
    }

    public void setPaymentResult(Integer paymentResult) {
        this.paymentResult = paymentResult;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Payment [account=" + account + ", date=" + date + ", description=" + description + ", id=" + id
                + ", paymentResult=" + paymentResult + ", prepareResult=" + prepareResult + ", sum=" + sum + "]";
    }

}
