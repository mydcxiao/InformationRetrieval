from bs4 import BeautifulSoup
import time
from time import sleep
import requests
from random import randint
from html.parser import HTMLParser
import json
import csv
from tqdm import tqdm
from requests.exceptions import ChunkedEncodingError

USER_AGENT = {'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36'}

class SearchEngine:
    @staticmethod
    def search(query, sleep=True):
        if sleep: # Prevents loading too many pages too soon
            time.sleep(randint(10, 20))
        temp_url = '+'.join(query.split()) #for adding + between words for the query
        url = 'https://www.bing.com/search?q=' + temp_url + '&count=30'
        soup = BeautifulSoup(requests.get(url, headers=USER_AGENT).text, "html.parser")
        new_results = SearchEngine.scrape_search_result(soup)
        return new_results
    
    @staticmethod
    def scrape_search_result(soup):
        raw_results = soup.find_all("li",  attrs={"class" : "b_algo"})
        results = []
        uniq_links = set()
        #implement a check to get only 10 results and also check that URLs must not be duplicated
        for result in raw_results:
            link = result.find('a').get('href')
            if link not in uniq_links:
                uniq_links.add(link)
                results.append(link)
            if len(results) == 10:
                break
        return results
    
    @staticmethod
    def process_url(url):
        url = url.rstrip(' /')
        if url.startswith('http://') or url.startswith('https://'):
            url.replace('http://', '').replace('https://', '')
        if url.startswith('www.'):
            url = url[4:]
        return url


def main():
    print("Running...")
    queries = []
    with open('100QueriesSet1.txt', 'r') as f:
       for query in f:
           queries.append(query.rstrip(' \n'))
    
    results = {}
    for i, query in tqdm(enumerate(queries)):
        results[query] = SearchEngine.search(query)
        if len(results[query]) < 10:
            print("Query", i + 1, "Less than 10 results for:", query)
    
    with open('hw1.json', 'w') as f:
        json.dump(results, f, indent=4)

    # with open('hw1.json', 'r') as f:
    #     results = json.load(f)
    
    with open('Google_Result1.json', 'r') as f:
        google_results = json.load(f)
    
    stats = []
    total_overlap, total_percent, total_rho = 0, 0, 0
    for i, query in tqdm(enumerate(queries)):
        google_urls = google_results[query]
        urls = results[query]
        google_urls_rank = {}
        for rank, url in enumerate(google_urls):
            url = SearchEngine.process_url(url)
            google_urls_rank[url] = rank + 1
        
        overlap, dist, is_same_rank = 0, 0, False
        for idx, url in enumerate(urls):
            url = SearchEngine.process_url(url)
            rank = idx + 1
            if url in google_urls_rank:
                overlap += 1
                dist += (rank - google_urls_rank[url]) ** 2
                if rank == google_urls_rank[url]:
                    is_same_rank = True
       
        if overlap == 0:
            rho = 0
        elif overlap == 1:
            rho = 1 if is_same_rank else 0
        else:
            rho = 1 - (6 * dist) / (overlap * (overlap ** 2 - 1))
        
        stats.append(["Query "+str(i+1), overlap, overlap/len(google_urls) * 100, rho])
        total_percent += overlap/len(google_urls) * 100
        total_overlap += overlap
        total_rho += rho
    
    with open('hw1.csv', 'w', newline='') as f:
        f.write("Queries, Number of Overlapping Results, Percent Overlap, Spearman Coefficient\n")
        for line in stats:
            f.write(", ".join([str(x) for x in line]) + "\n")
        f.write(f"Averages, {total_overlap/len(queries)}, {total_percent/len(queries)}, {total_rho/len(queries)}")
    
    print("Done!")

if __name__ == '__main__':
    main()


    