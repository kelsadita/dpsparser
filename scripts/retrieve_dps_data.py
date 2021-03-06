from datetime import date, timedelta
import requests

DPS_URL = 'https://dps.usc.edu/files/'
START_DATE = date(2018, 5, 4)
END_DATE = date(2018, 6, 12)
STATUS_CODE_OK = 200
delta = END_DATE - START_DATE         # timedelta

file_name_path = "/Users/kalpesh/Desktop/dps-pdf/"
for day in range(delta.days + 1):
    current_date = (START_DATE + timedelta(days=day))

    month = current_date.strftime('%m')
    year = current_date.strftime('%Y')
    date = current_date.strftime('%d')

    two_digit_year = current_date.year % 100

    date_url = year + '/' + month + '/'
    file_name = month + date + str(two_digit_year) + '.pdf'
    url = DPS_URL + date_url + file_name
    response = requests.get(url)
    if response.status_code != STATUS_CODE_OK:
        print(url + " no pdf generated")
    else:
        with open(file_name_path + file_name, "wb") as f:
            f.write(response.content)
