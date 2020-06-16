job("self_imagebuilder1"){
        description("this job will copy the file in your os version and push image to docker hub")
        scm {
                 github('nikhilgubbi/dockerauto' , 'master')
             }
        triggers {
                scm("* * * * *")
                
        }
         label("s1")


        steps {
        shell('''sudo cp * /html/

sudo docker build -t nikhil/http:latest .

sudo docker push nikhil/http''')
      }
}

job("self_pod_managment1"){
        description("this will creat deploymet for website and expose deployment")
        
        triggers {
        upstream {
    upstreamProjects("self_imagebuilder1")
    threshold("Fail")
        }
        }
        label("s1")

        steps {
        shell('''if sudo kubectl get deployment | grep myweb
then
echo " updating"
else
sudo kubectl create deployment myweb --image=nikhil/http
sudo kubectl autoscale deployment myweb --min=10 --max=15 --cpu-percent=80
fi

if sudo kubectl get deployment -o wide | grep latest
then 
sudo kubectl set image deployment myweb http=nikhil/http
else
sudo kubectl set image deployment myweb http=nikhil/http:latest
fi




if sudo kubectl get service | grep myweb
then 
echo "service exist"
else
sudo kubectl expose deployment myweb --port=80 --type=NodePort
fi ''')
      }
}


job("self_finaltesting1"){
        description("this job will test the env")
        
        triggers {
                upstream {
    upstreamProjects("self_pod_managment1")
    threshold("Fail")
   } 
        }
        label("s1")


        steps {
        shell('''if sudo kubectl get deployment | grep myweb
then
echo " All good"
else
cd /nikhil/
python3 mail.py

fi
''')
      }
}
