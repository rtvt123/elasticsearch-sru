# Elasticsearch SRU Plugin

The SRU plugin for Elasticsearch is a plugin to add Search/Retrieval via URL (SRU) endpoints to
Elasticsearch REST HTTP interface. It provides the capability to execute Contextual Query Language (CQL)
on Elasticsearch indices.

Search/Retrieve via URL (SRU) is a standard protocol for search queries, 
utilizing Contextual Query Language (CQL), a standard language for expressing queries.

SRU refers to a suite of specficiations that has been approved as an OASIS Standard, referred to as 
searchRetrieve 1.0. See [searchRetrieve Version 1.0](https://www.oasis-open.org/news/announcements/searchretrieve-version-1-0-oasis-standard-published).

The Contextual Query Language (CQL), is a language for expressing queries to information retrieval systems 
such as web indexes, bibliographic catalogs and museum collection information. The design objective is that 
queries be human readable and writable, and that the language be intuitive while maintaining the expressiveness 
of more complex languages.

Traditionally, query languages have fallen into two camps: on the one hand, powerful, expressive languages, 
not easily readable nor writable by non-experts (e.g. SQL, PQF, and XQuery), on the other hand, simple and intuitive 
languages not powerful enough to express complex concepts (e.g. CCL and google). 
CQL tries to combine simplicity and intuitiveness of expression for simple, every day queries, 
with the richness of more expressive languages to accomodate complex concepts when necessary.

(text taken from [Library of Congress](http://www.loc.gov/standards/sru/cql/index.html))

## Versions

| Elasticsearch version    | Plugin     | Release date |
| ------------------------ | -----------| -------------|
| 1.3.1                    | 1.3.0.0    | Aug 10, 2014 |

## Checksum

| File                                         | SHA1                                     |
| ---------------------------------------------| -----------------------------------------|
| elasticsearch-sru-1.3.0.0-plugin.zip         | 14709bae036a594be3eb85a57dc49742e69e3e06 |

## Installation

    ./bin/plugin --install sru --url http://xbib.org/repository/org/xbib/elasticsearch/plugin/elasticsearch-sru/1.3.0.0/elasticsearch-sru-1.3.0.0-plugin.zip

Do not forget to restart the node after installing.

## Project docs

The Maven project site is available at [Github](http://xbib.github.io/elasticsearch-sru)

## Issue

Posting issues is most welcome at [Github](http://github.com/xbib/elasticsearch/issues)

# IMPORTANT NOTICE

The implementation of the SRU plugin has just begun. Currently, XML format is not implemented. 
All SRU responses are given back in native Elasticsearch JSON format. 
XML, XSL stylesheets, and optional XML schema validation will be added later.

# Example

Consider the document

    PUT /test/test/1
    {
        "dc" : {
            "title" : "Köln - die schönste Stadt der Welt",
            "date" : 2012,
            "format" : "online",
            "type" : "electronic"
        }
    }


The SRU query

    curl -XGET '0:9200/test/_sru?operation=searchRetrieve&version=2.0&query=dc.title=k%c3%b6ln&startRecord=1&maximumRecords=10&filter=dc.format=online%20and%20dc.date=2012&facetLimit=10:dc.format&pretty'

or, if you prefer Java API

       SearchRetrieveRequest cqlRequest = new SearchRetrieveRequest()
                .setStartRecord(1)
                .setMaximumRecords(10)
                .setQuery("dc.title=Köln")
                .setFilter("dc.format = online and dc.date = 2012")
                .setFacetLimit("10:dc.format");

will give the result

    {
      "took" : 11,
      "timed_out" : false,
      "_shards" : {
        "total" : 5,
        "successful" : 5,
        "failed" : 0
      },
      "hits" : {
        "total" : 1,
        "max_score" : 0.095891505,
        "hits" : [ {
          "_index" : "test",
          "_type" : "test",
          "_id" : "1",
          "_score" : 0.095891505,
          "_source":{
        "dc" : {
            "title" : "Köln - die schönste Stadt der Welt",
            "date" : 2012,
            "format" : "online",
            "type" : "electronic"
        }
    }
    
        } ]
      },
      "aggregations" : {
        "dc.format" : {
          "buckets" : [ {
            "key" : "online",
            "doc_count" : 1
          } ]
        }
      }
    }


# License

elasticsearch-sru - Elasticsearch plugin for Search/Retrieval via URL

Copyright (C) 2014 Jörg Prante

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.