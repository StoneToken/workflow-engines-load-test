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

    private long id;
    private long sum;
    private String account;
    private String description;
    private String date;
    private String prepareResult;
    private String paymentResult;

    public long getId() {
        return id;
    }
    public String getPrepareResult() {
        return prepareResult;
    }
    public void setPrepareResult(String prepareResult) {
        this.prepareResult = prepareResult;
    }
    public String getPaymentResult() {
        return paymentResult;
    }
    public void setPaymentResult(String paymentResult) {
        this.paymentResult = paymentResult;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getDebit() {
        return debit;
    }
    public void setDebit(long debit) {
        this.debit = debit;
    }
    public long getCredit() {
        return credit;
    }
    public void setCredit(long credit) {
        this.credit = credit;
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

    @Override
    public String toString() {
        return "Payment [credit=" + credit + ", date=" + date + ", debit=" + debit + ", description=" + description
                + ", id=" + id + ", paymentResult=" + paymentResult + ", prepareResult=" + prepareResult + "]";
    }
    public long getSum() {
        return sum;
    }
    public void setSum(long sum) {
        this.sum = sum;
    }
    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }

}
