from django.shortcuts import render
from index.models import WelcomeMessage 
from django.db.models import Max
from django.core.exceptions import EmptyResultSet
import random

def index(request):
    try:
        msg = get_random_message()
    except EmptyResultSet:
        return render(request, "index.html", { "message": WelcomeMessage(text = "Welcome to the django sample app") })
    return render(request, "index.html", { "message": msg })

def get_random_message():
    max_id = WelcomeMessage.objects.all().aggregate(max_id=Max("id"))['max_id']
    if max_id == None:
        raise EmptyResultSet("no welcome messages found in database")
    while True:
        pk = random.randint(1, max_id)
        msg = WelcomeMessage.objects.filter(pk=pk).first()
        if msg:
            return msg
