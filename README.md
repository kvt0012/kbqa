# A comparative study of question answering over knowledge bases

![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54) 	![PyTorch](https://img.shields.io/badge/PyTorch-%23EE4C2C.svg?style=for-the-badge&logo=PyTorch&logoColor=white)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
- This repository is a implementation of the framework for a comparative study of question answering over knowledge bases.

- In this work, we are also releasing a dataset for COVID-19. The dataset is provided in folder [COVID_data](https://github.com/tamlhp/kbqa/tree/main/data/COVID) in this repository. More details about dataset are provided below.

<!-- <!-- *Paper*: []() -->
#### Citation 
```
@Article{Tran2021KBQA,
  Title= {A comparative study of question answering over knowledge bases},
  Author= {Khiem Vinh Tran, Hao Phu Phan, Khang Nguyen Duc Quach, Ngan Luu-Thuy Nguyen, Jun Jo, Thanh Tam Nguyen},
  booktitle={Advanced Data Mining and Applications},
  Year={2022},
  Publisher={Springer International Publishing},
}
```
# Framework

## Dataset format

```json
{
  "id":"6",
  "question":[ {
         "language":"en",
         "string":"Where is the first case in Vietnam?  ",
         "keywords":"first case, COVID-19, Vietnam "
      }, {
         "language":"vi",
         "string":"Truong hop ca nhiem COVID-19 dau tien cua Viet Nam la o dau?",
         "keywords":"Ca nhiem dau tien, COVID-19, Viet Nam"
      }],
  "query":{
      "sparql":"SELECT DISTINCT ?uri WHERE { <http://dbpedia.org/resource/COVID-19_pandemic_in_Vietnam> <http://dbpedia.org/property/firstCase> ?uri }"
  },
  "answers":[{"head":{"vars":["uri"]},
         "results":{"bindings":[{"uri":{
                     "type":"uri",
                     "value":"http://dbpedia.org/resource/Ho_Chi_Minh_City"
                  }}]}}
  ]
}
```

## Enviroment
` pip install -r requirement.txt` 
## Preprocess data


`python main.py --inp in/ --output out/ `

`--data` : Data input configuration

`--output` : Data output

##  Usage

`python main.py --data --output`
- ```Training:``` python main.py 

- ```Validate:``` python main.py --task validate

- ```Test:``` python main.py --task test

## Support 
Please raise potential bugs on github. If you have a research related question, please send it to this email(vinhkhiemt135@gmail.com)


