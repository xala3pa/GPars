// GPars (formerly GParallelizer)
//
// Copyright © 2008-10  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.samples.dataflow

import groovyx.gpars.ThreadPool
import groovyx.gpars.dataflow.DataFlows

/* Demonstrating how to process the results of various threads
(here fetching stock prices in parallel) while allowing maximum
concurrency with the help of DataFlows that store the single
results.
All synchronization logic is hidden in the access to DataFlows.
@author Dierk Koenig
 */

/** Fetch the stock price for the end of that year from the yahoo REST service.
 * @return price as double or 0 if call failed.      */
def getYearEndClosing(String stock, int year) {
    def url = "http://ichart.finance.yahoo.com/table.csv?s=$stock&amp;a=11&amp;b=01&amp;c=$year&amp;d=11&amp;e=31&amp;f=$year&amp;g=m;ignore=.csv"
    try {
        def data = url.toURL().text
        return data.split("\n")[1].split(",")[4].toDouble()
    } catch (all) {
        println "Could not get $stock, assuming value 0. $all.message"
        return 0
    }
}

def stocks = ['AAPL', 'GOOG', 'IBM', 'JAVA', 'MSFT']
def price = new DataFlows() // key: stock name, value: price

ThreadPool.withPool {
    // spawn a thread per stock that stores the result in its DataFlow
    stocks.each({stock ->
        price[stock] = getYearEndClosing(stock, 2008)
    }.async())

// Even though max() goes through the DataFlows in given order
// the fetching threads can run in full parallel
    def topStock = stocks.max { price[it] }
    println "Top stock is $topStock with price ${price[topStock]}"
}

