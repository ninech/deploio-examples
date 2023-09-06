from django.core.management.base import BaseCommand
from django.contrib.auth.models import User

class Command(BaseCommand):
    help = 'Creates an admin user if none exists'

    def handle(self, *args, **kwargs):
        self.stdout.write("blablubb")
