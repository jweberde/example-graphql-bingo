import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CardCreatedResponse } from 'src/app/model/card-created-response';
import { CardService } from 'src/app/service/card.service';

@Component({
  selector: 'app-start',
  templateUrl: './start.component.html',
  styleUrls: ['./start.component.scss'],
})
export class StartComponent implements OnInit {
  public sessionForm: FormGroup;
  sessionFormSubmitted = false;
  loading = false;

  public newCard!: CardCreatedResponse;

  constructor(
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar,
    private cardService: CardService
  ) {
    this.sessionForm = this.formBuilder.group({
      name: [null, [Validators.required, Validators.maxLength(255)]],
    });
  }

  ngOnInit(): void {}

  submit(): void {
    this.loading = true;
    this.sessionFormSubmitted = true;
    if (!this.sessionForm.valid) {
      this.loading = false;
      return;
    }

    this.sessionForm.disable();

    const owner = this.sessionForm.get('name')?.value;
    this.cardService.createNewCard(owner, true).subscribe(
      (newCard: CardCreatedResponse) => {
        this.newCard = newCard;
      },
      (error) => {
        console.error('Mutation Error', error);
        this.loading = false;
        this.sessionForm.enable();
        this.snackBar.open('Something went wrong, try again: ' + error, '', {
          duration: 2000,
        });
      }
    );
  }
}
