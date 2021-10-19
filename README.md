# A comparative study of question answering over knowledge bases

[![image](https://img.shields.io/badge/Made%20with-Python-1f425f.svg)](https://www.python.org/)
- This repository is a implementation of the Framework for comparative study in our paper:
[A comparative study of question answering over knowledge bases](). 

- In this work, we are also releasing a dataset for COVID-19. The dataset is provided in folder [COVID_data](https://github.com/tamlhp/kbqa/tree/master/covid_data) in this repository. More details about dataset are provided below.

- Links related to this work:
  - Paper: 
  - Dataset and codes: https://github.com/tamlhp/kbqa/

*Paper*: []()
#### Citation 
```
@Article{Tran,
  Title                    = {A comparative study of question answering over knowledge bases},
  Author                   = {},
  Journal                  = {},
  Year                     = {},
  volume                   = {},
  number                    = {}
}
```
# Framework

## Dataset

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
Extract fame from video and detect face in frame to save *.jpg image.

`python main.py --inp in/ --output out/ `

`--data` : Data input configuration

`--output` : Data output

##  Usage

`python main.py --data --output`
- ```Training:``` python main.py 

- ```Validate:``` python main.py --task validate

- ```Test:``` python main.py --task test



## References
[1] 

