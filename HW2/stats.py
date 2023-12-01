import pandas as pd
import http

with open('fetch_nytimes.csv', 'r', encoding='UTF-8') as f:
    df = pd.read_csv(f, header=0)
    attempts = df.shape[0]
    successes = df[(200 <= df['Status']) & (df['Status'] < 300)].shape[0]
    failures = df[df['Status'] >= 300].shape[0]
    status_stats = df.groupby('Status').count().sort_index().to_dict()['URL']

with open('visit_nytimes.csv', 'r', encoding='UTF-8') as f:
    df = pd.read_csv(f, header=0)
    total_urls = df['#Outlinks'].sum()
    ls_1k = df[df['Size(Bytes)'] < 1024].shape[0]
    ls_10k = df[(1024 <= df['Size(Bytes)']) & (df['Size(Bytes)'] < 10240)].shape[0]
    ls_100k = df[(10240 <= df['Size(Bytes)']) & (df['Size(Bytes)'] < 102400)].shape[0]
    ls_1m = df[(102400 <= df['Size(Bytes)']) & (df['Size(Bytes)'] < 1024 * 1024)].shape[0]
    gt_1m = df[df['Size(Bytes)'] >= 1024 * 1024].shape[0]
    type_stats = df.groupby('Type').count().sort_index().to_dict()['URL']

with open('urls_nytimes.csv', 'r', encoding='UTF-8') as f:
    df = pd.read_csv(f, header=0)
    # all_urls = df.shape[0]
    unique_urls = df['URL'].unique().shape[0]
    uq_df = df.drop_duplicates(subset=['URL'])
    # assert all_urls == unique_urls, 'Duplicate URLs found'
    # unique_inlinks = df[df['Indicator'] == 'OK'].shape[0]
    # unique_outlinks = df[df['Indicator'] == 'N_OK'].shape[0]
    unique_inlinks = uq_df[uq_df['Indicator'] == 'OK'].shape[0]
    unique_outlinks = uq_df[uq_df['Indicator'] == 'N_OK'].shape[0]
    assert unique_urls == unique_inlinks + unique_outlinks, 'Inconsistent URL counts'

with open('CrawlReport_nytimes.txt', 'w') as f:
    f.write('Name: Yuhang Xiao\n')
    f.write('USC ID: 6913860906\n')
    f.write('News site crawled: nytimes.com\n')
    f.write('Number of threads: 16\n')
    f.write('\n')
    f.write('Fetch Statistics\n')
    f.write('================\n')
    f.write(f'fetches attempted: {attempts}\n')
    f.write(f'fetches succeeded: {successes}\n')
    f.write(f'fetches failed or aborted: {failures}\n')
    f.write('\n')
    f.write('Outgoing URLs:\n')
    f.write('==============\n')
    f.write(f'Total URLs extracted: {total_urls}\n')
    f.write(f'# unique URLs extracted: {unique_urls}\n')
    f.write(f'# unique URLs within News Site: {unique_inlinks}\n')
    f.write(f'# unique URLs outside News Site: {unique_outlinks}\n')
    f.write('\n')
    f.write('Status Codes:\n')
    f.write('=============\n')
    for k, v in status_stats.items():
        f.write(f'{k} {http.HTTPStatus(k).phrase}: {v}\n')
    f.write('\n')
    f.write('File Sizes:\n')
    f.write('===========\n')
    f.write(f'< 1KB: {ls_1k}\n')
    f.write(f'1KB ~ <10KB: {ls_10k}\n')
    f.write(f'10KB ~ <100KB: {ls_100k}\n')
    f.write(f'100KB ~ <1MB: {ls_1m}\n')
    f.write(f'>= 1MB: {gt_1m}\n')
    f.write('\n')
    f.write('Content Types:\n')
    f.write('==============\n')
    for k, v in type_stats.items():
        f.write(f'{k}: {v}\n')