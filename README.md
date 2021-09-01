# kbqa
Knowledge base Question Answering
# Framework
## Enviroment
` pip install -r requirement.txt` 
## Preprocess data
Extract fame from video and detect face in frame to save *.jpg image.

`python extrac_face.py --inp in/ --output out/ --worker 1 --duration 4`

`--data` : Data input configuration

`--output` : Data output

##  Train

`python main.py --data --output`



## References
[1] https://github.com/nii-yamagishilab/Capsule-Forensics-v2



