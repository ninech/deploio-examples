from django.db import models

class WelcomeMessage(models.Model):
    text = models.CharField(max_length=160)
